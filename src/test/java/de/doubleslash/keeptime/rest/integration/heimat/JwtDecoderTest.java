// Copyright 2026 doubleSlash Net Business GmbH
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class JwtDecoderTest {

   private static JwtDecoder.JWTTokenAttributes tokenExpiringAt(LocalDateTime expiration) {
      return new JwtDecoder.JWTTokenAttributes("{}", "{}", expiration);
   }

   @Test
   void shouldNotBeExpiredWhenNowIsBeforeExpiration() {
      LocalDateTime expiration = LocalDateTime.of(2025, 6, 15, 10, 30, 0);
      JwtDecoder.JWTTokenAttributes token = tokenExpiringAt(expiration);

      assertFalse(JwtDecoder.isExpired(token, expiration.minusNanos(1)));
   }

   @Test
   void shouldNotBeExpiredWhenNowIsExactlyExpiration() {
      LocalDateTime expiration = LocalDateTime.of(2025, 6, 15, 10, 30, 0);
      JwtDecoder.JWTTokenAttributes token = tokenExpiringAt(expiration);

      assertFalse(JwtDecoder.isExpired(token, expiration));
   }

   @Test
   void shouldBeExpiredWhenNowIsAfterExpiration() {
      LocalDateTime expiration = LocalDateTime.of(2025, 6, 15, 10, 30, 0);
      JwtDecoder.JWTTokenAttributes token = tokenExpiringAt(expiration);

      assertTrue(JwtDecoder.isExpired(token, expiration.plusNanos(1)));
   }




}
