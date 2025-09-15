// Copyright 2019 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.common;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateFormatter {
   private static DateTimeFormatter dayDateFormatter = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy");
   private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

   private static Locale systemLocale = Locale.getDefault();

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

   public static void setSystemLocale(Locale locale) {
      systemLocale = locale;
   }

   public static Locale getSystemLocale() {
      return systemLocale;
   }

   public static void applySystemLocaleOnDate(DatePicker datePicker) {
      DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(systemLocale);

      datePicker.setConverter(new StringConverter<>() {
         @Override
         public String toString(LocalDate date) {
            return (date != null) ? formatter.format(date) : "";
         }

         @Override
         public LocalDate fromString(String s) {
            try {
               return (s != null && !s.isEmpty()) ? LocalDate.parse(s, formatter) : null;
            } catch (DateTimeParseException e) {
               return null;
            }
         }
      });

      datePicker.setPromptText(formatter.format(LocalDate.now()));
   }
}
