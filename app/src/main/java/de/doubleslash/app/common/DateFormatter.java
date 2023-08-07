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

package de.doubleslash.app.common;

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
