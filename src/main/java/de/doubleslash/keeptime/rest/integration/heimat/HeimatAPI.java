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

import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTime;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HeimatAPI {

   private final RestClient restClient;

   public HeimatAPI(final String baseUrl, final String bearerToken) {
      restClient = RestClient.builder()
                             .baseUrl(baseUrl + "/heimat-core/api/v1/")
                             .defaultHeader("X-Client-Identifier", "KeepTime")
                             .defaultHeader("Authorization", "Bearer " + bearerToken)
                             .defaultHeader("Accept", "application/json")
                             .build();
   }

   public boolean isLoginValid() {
      getMyTasks();
      return true;
   }

   public List<HeimatTask> getMyTasks() {
      return getMyTasks(null);
   }

   // GET /my/tasks
   public List<HeimatTask> getMyTasks(final LocalDate forDate) {
      return restClient.get().uri(uriBuilder -> {
         UriBuilder builder = uriBuilder.path("/my/tasks");
         if (forDate != null) {
            builder.queryParam("date", forDate.format(DateTimeFormatter.ISO_DATE));
         }
         return builder.build();
      }).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, (request, response) -> {
         throw new UnauthorizedException();
      }).body(new ParameterizedTypeReference<>() {});
   }

   public List<HeimatTime> getMyTimes() {
      return getMyTimes(null);
   }

   // GET /my/times
   public List<HeimatTime> getMyTimes(final LocalDate forDate) {
      return restClient.get().uri(uriBuilder -> {
         UriBuilder builder = uriBuilder.path("/my/times");
         if (forDate != null) {
            builder.queryParam("date", forDate.format(DateTimeFormatter.ISO_DATE));
         }
         return builder.build();
      }).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, (request, response) -> {
         throw new UnauthorizedException();
      }).body(new ParameterizedTypeReference<>() {});
   }

   // POST /my/times
   public void addMyTime(final HeimatTime heimatTime) {
      restClient.post()
                .uri(uriBuilder -> {
                   UriBuilder builder = uriBuilder.path("/my/times");
                   return builder.build();
                })
                .header("Content-Type", "application/json")
                .body(heimatTime)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals, (request, response) -> {
                   throw new UnauthorizedException();
                })
                .toEntity(String.class);
   }

   // DELETE /my/times/{id}
   public void deleteMyTime(final long timeId) {
      restClient.delete().uri(uriBuilder -> {
         UriBuilder builder = uriBuilder.path("/my/times/" + timeId);
         return builder.build();
      }).retrieve().onStatus(HttpStatus.UNAUTHORIZED::equals, (request, response) -> {
         throw new UnauthorizedException();
      }).toEntity(String.class);
   }
}
