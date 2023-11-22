package de.doubleslash.keeptime.REST_API.controller;

public class ProjectDTO {
   private long id;

   public ProjectDTO(final long id) {
      this.id = id;
   }

   public long getId() {
      return id;
   }

   public void setId(final long id) {
      this.id = id;
   }
}