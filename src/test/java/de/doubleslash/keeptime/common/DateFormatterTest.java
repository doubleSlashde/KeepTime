// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DateFormatterTest {

   @Test
   public void zeroSecondsBetweenTest() {
      final LocalDateTime startDate = LocalDateTime.now();
      final LocalDateTime endDate = startDate.plusNanos(10000);

      final long secondsBewtween = DateFormatter.getSecondsBewtween(startDate, endDate);
      assertThat(secondsBewtween, Matchers.is(0l));

      final long secondsBewtweenSwitched = DateFormatter.getSecondsBewtween(endDate, startDate);
      assertThat(secondsBewtweenSwitched, Matchers.is(0l)); // why??
   }
}
