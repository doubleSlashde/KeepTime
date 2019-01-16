package de.doubleslash.keeptime.common;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class RealDateProvider implements DateProvider {
   @Override
   public LocalDateTime dateTimeNow() {
      return LocalDateTime.now();
   }
}
