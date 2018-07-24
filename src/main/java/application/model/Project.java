package application.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javafx.scene.paint.Color;

@Entity
@Table(name = "Project")
public class Project {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   public String name;

   // TODO color mapper
   @Convert(converter = ColorConverter.class, disableConversion = false)
   public Color color;

   public boolean isWork;

   public boolean isDefault;

   public boolean isEnabled;

   public Project() {

   }

   public Project(final String name, final Color color, final boolean isWork) {
      super();
      this.name = name;
      this.color = color;
      this.isWork = isWork;
      this.isEnabled = true;
   }

   @Override
   public String toString() {
      return "Project [name=" + name + ", color=" + color + ", isWork=" + isWork + ", isDefault=" + isDefault + "]";
   }

}
