package de.doubleslash.keeptime.rest.integration.heimat.model;

public record HeimatTask(
      long id, // int64
      String name,
      String taskHolderName,
      String taskHolderType,
      boolean isFavorite,
      String bookingHint,
      boolean isStartAndEndTimeRequired,
      boolean isNoteOptional
) {}
