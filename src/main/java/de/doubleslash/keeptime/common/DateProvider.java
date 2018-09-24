package de.doubleslash.keeptime.common;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateProvider {

   LocalDateTime dateTimeNow();

   LocalDate dateNow();

}
