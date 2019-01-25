package de.doubleslash.keeptime.common;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
   private static DateTimeFormatter dayDateFormatter = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy");
   private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

   private DateFormatter() {
      throw new IllegalStateException("Utility class: DateFormatter");
   }

   public static String secondsToHHMMSS(final long currentWorkSeconds) {
      final int hours = (int) (currentWorkSeconds / 3600);
      final int minutes = (int) ((currentWorkSeconds % 3600) / 60);

      final int sec = (int) (currentWorkSeconds % 3600 % 60);

      final Object hoursString = hours > 9 ? hours : "0" + hours;
      final Object minutesString = minutes > 9 ? minutes : "0" + minutes;
      final Object secondsString = sec > 9 ? sec : "0" + sec;

      final String timeString = hoursString + ":" + minutesString + ":" + secondsString;
      return timeString;
   }

   public static long getSecondsBewtween(final LocalDateTime startDate, final LocalDateTime endDate) {
      return Math.abs(Duration.between(startDate, endDate).getSeconds());
   }

   public static String toDayDateString(final LocalDate newvalue) {
      return newvalue.format(dayDateFormatter);
   }

   public static String toTimeString(final LocalDateTime localDateTime) {
      return localDateTime.format(timeFormatter);
   }

}
