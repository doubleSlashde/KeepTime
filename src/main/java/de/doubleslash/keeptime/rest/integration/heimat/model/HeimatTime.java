package de.doubleslash.keeptime.rest.integration.heimat.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record HeimatTime(
      long taskId, // int64
      @JsonFormat(pattern = "yyyy-MM-dd")
      LocalDate date, // 2024-01-01
      LocalTime start, // 23:59
      LocalTime end, // 23:59
      int durationInMinutes, // int32
      String note,
      long id // int64
) {}
