// Copyright 2025 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.rest.integration.heimat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

public class JwtDecoder {

   public record JWTTokenAttributes(
         String header,
         String payload,
         LocalDateTime expiration
   ) { }


   public static JWTTokenAttributes parse(String bearerToken) {
      String token = removeBearerPrefix(bearerToken);

      String[] parts = token.split("\\.");
      if (parts.length != 3) {
         throw new IllegalArgumentException("Invalid JWT token format");
      }

      String header = new String(Base64.getUrlDecoder().decode(parts[0]));
      String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> claims = null;
      try {
         claims = mapper.readValue(payload, Map.class);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }

      final LocalDateTime expiration = LocalDateTime.parse((String) claims.get("expiration"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

      return new JWTTokenAttributes(header, payload, expiration);
   }

   public static boolean isExpired(JWTTokenAttributes token, LocalDateTime localDateTimeNow) {
      return token.expiration.isAfter(localDateTimeNow);
   }

   private static String removeBearerPrefix(String token) {
      return token.startsWith("Bearer ") ? token.substring(7) : token;
   }
}
