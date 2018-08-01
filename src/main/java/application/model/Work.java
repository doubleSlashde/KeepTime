package application.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Work")
public class Work {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   private LocalDate creationDate;
   private LocalDateTime startTime;
   private LocalDateTime endTime;

   @ManyToOne
   private Project project;
   @Lob
   private String notes;

   public Work() {}

   public Work(final LocalDateTime startTime, final LocalDateTime endTime, final Project project, final String notes) {
      super();
      this.creationDate = LocalDate.now();
      this.startTime = startTime;
      this.endTime = endTime;
      this.project = project;
      this.notes = notes;
   }

   public long getId() {
      return id;
   }

   public LocalDate getCreationDate() {
      return creationDate;
   }

   public void setCreationDate(final LocalDate creationDate) {
      this.creationDate = creationDate;
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
