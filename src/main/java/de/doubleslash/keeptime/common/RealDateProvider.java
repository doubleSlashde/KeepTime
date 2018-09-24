package de.doubleslash.keeptime.common;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class RealDateProvider implements DateProvider {
   @Override
   public LocalDateTime dateTimeNow() {
      return LocalDateTime.now();
   }

   @Override
   public LocalDate dateNow() {
      return LocalDate.now();
   }
}
