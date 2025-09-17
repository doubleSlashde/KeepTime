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

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import javafx.scene.control.DatePicker;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class DateFormatterTest {

   @Test
   void zeroSecondsBetweenTest() {
      final LocalDateTime startDate = LocalDateTime.now();
      final LocalDateTime endDate = startDate.plusNanos(10000);

      final long secondsBewtween = DateFormatter.getSecondsBewtween(startDate, endDate);
      assertThat(secondsBewtween, Matchers.is(0l));

      final long secondsBewtweenSwitched = DateFormatter.getSecondsBewtween(endDate, startDate);
      assertThat(secondsBewtweenSwitched, Matchers.is(0l)); // why??
   }

   @Test
   void applySystemLocaleShouldSetDatePickerLocale() {
      // GIVEN
      Locale defaultLocale = Locale.getDefault();
      Locale.setDefault(Locale.ENGLISH);
      DateFormatter.setSystemLocale(Locale.GERMAN);
      DatePicker datePicker = new DatePicker();
      LocalDate date = LocalDate.of(2024, 6, 3);

      // WHEN
      DateFormatter.applySystemLocaleOnDate(datePicker);
      datePicker.setValue(date);

      // THEN
      String shown = datePicker.getEditor().getText();
      assertTrue(shown.contains("Montag"));
      assertTrue(shown.contains("06.2024"));

      Locale.setDefault(defaultLocale);
   }
}
