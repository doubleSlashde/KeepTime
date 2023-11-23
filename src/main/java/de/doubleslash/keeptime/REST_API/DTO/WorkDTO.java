package de.doubleslash.keeptime.REST_API.DTO;

import java.time.LocalDateTime;

public class WorkDTO {
   private long id;
   private LocalDateTime startTime;
   private LocalDateTime endTime;
   private ProjectDTO project;
   private String notes;

   public WorkDTO(long id, LocalDateTime startTime, LocalDateTime endTime, ProjectDTO project, String notes) {
      this.id = id;
      this.startTime = startTime;
      this.endTime = endTime;
      this.project = project;
      this.notes = notes;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public LocalDateTime getStartTime() {
      return startTime;
   }

   public void setStartTime(LocalDateTime startTime) {
      this.startTime = startTime;
   }

   public LocalDateTime getEndTime() {
      return endTime;
   }

   public void setEndTime(LocalDateTime endTime) {
      this.endTime = endTime;
   }

   public ProjectDTO getProject() {
      return project;
   }

   public void setProject(ProjectDTO project) {
      this.project = project;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }
}
