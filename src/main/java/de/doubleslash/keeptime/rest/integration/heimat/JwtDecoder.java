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
   ) {}


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

   private static String removeBearerPrefix(String token) {
      return token.startsWith("Bearer ") ? token.substring(7) : token;
   }
}
