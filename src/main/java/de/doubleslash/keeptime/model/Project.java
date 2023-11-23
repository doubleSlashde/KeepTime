package de.doubleslash.keeptime.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;

import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import javafx.scene.paint.Color;

@Entity
@Table(name = "Project")
public class Project {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   @NotEmpty(message = "Name must not be null or empty")
   private String name;

   @Lob
   private String description;

   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color color;

   private boolean isWork;
   private boolean isDefault;
   private boolean isEnabled;

   @PositiveOrZero(message = "Index must not be negative")
   private int index;

   public Project() {
      // Needed for jpa
   }

   public Project(String name, String description, Color color, boolean isWork, int index, boolean isDefault) {
      this.name = name;
      this.description = description;
      this.color = color;
      this.isWork = isWork;
      this.isDefault = isDefault;
      this.isEnabled = true;
      this.index = index;
   }

   public Project(String name, String description, Color color, boolean isWork, int index) {
      this(name, description, color, isWork, index, false);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Color getColor() {
      return color;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public boolean isWork() {
      return isWork;
   }

   public void setWork(boolean isWork) {
      this.isWork = isWork;
   }

   public boolean isDefault() {
      return isDefault;
   }

   public void setDefault(boolean isDefault) {
      this.isDefault = isDefault;
   }

   public boolean isEnabled() {
      return isEnabled;
   }

   public void setEnabled(boolean isEnabled) {
      this.isEnabled = isEnabled;
   }

   public long getId() {
      return id;
   }

   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   @Override
   public String toString() {
      return "Project [id=" + id + ", name=" + name + ", description=" + description + ", color=" + color + ", isWork="
            + isWork + ", isDefault=" + isDefault + ", isEnabled=" + isEnabled + ", index=" + index + "]";
   }
}