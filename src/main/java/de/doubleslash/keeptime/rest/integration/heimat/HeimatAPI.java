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
                });
   }

   // DELETE /my/times/{id}

}
