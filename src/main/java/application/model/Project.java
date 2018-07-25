package application.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import application.model.persistenceConverter.ColorConverter;
import javafx.scene.paint.Color;

@Entity
@Table(name = "Project")
public class Project {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   private String name;

   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color color;

   private boolean isWork;

   private boolean isDefault;

   private boolean isEnabled;

   public Project() {
      // Needed for jpa
   }

   public Project(final String name, final Color color, final boolean isWork) {
      super();
      this.name = name;
      this.color = color;
      this.isWork = isWork;
      this.isEnabled = true;
   }

   public String getName() {
      return name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public Color getColor() {
      return color;
   }

   public void setColor(final Color color) {
      this.color = color;
   }

   public boolean isWork() {
      return isWork;
   }

   public void setWork(final boolean isWork) {
      this.isWork = isWork;
   }

   public boolean isDefault() {
      return isDefault;
   }

   public void setDefault(final boolean isDefault) {
      this.isDefault = isDefault;
   }

   public boolean isEnabled() {
      return isEnabled;
   }

   public void setEnabled(final boolean isEnabled) {
      this.isEnabled = isEnabled;
   }

   public long getId() {
      return id;
   }

   @Override
   public String toString() {
      return "Project [name=" + name + ", color=" + color + ", isWork=" + isWork + ", isDefault=" + isDefault
            + ", isEnabled=" + isEnabled + "]";
   }

}
