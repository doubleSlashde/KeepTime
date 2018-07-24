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

   public LocalDateTime startTime;
   public LocalDateTime endTime;

   @OneToOne
   public Project project;
   public String notes;

   public Work(final LocalDateTime startTime, final LocalDateTime endTime, final Project project, final String notes) {
      super();
      this.startTime = startTime;
      this.endTime = endTime;
      this.project = project;
      this.notes = notes;
   }

}
