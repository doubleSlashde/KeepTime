package application.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Work")
public class Work {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   private LocalDateTime startTime;
   private LocalDateTime endTime;

   @OneToOne
   private final Project project;
   private String notes;

   public Work(final LocalDateTime startTime, final LocalDateTime endTime, final Project project, final String notes) {
      super();
      this.startTime = startTime;
      this.endTime = endTime;
      this.project = project;
      this.notes = notes;
   }

   public long getId() {
      return id;
   }

   public LocalDateTime getStartTime() {
      return startTime;
   }

   public void setStartTime(final LocalDateTime startTime) {
      this.startTime = startTime;
   }

   public LocalDateTime getEndTime() {
      return endTime;
   }

   public void setEndTime(final LocalDateTime endTime) {
      this.endTime = endTime;
   }

   public Project getProject() {
      return project;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(final String notes) {
      this.notes = notes;
   }

}
