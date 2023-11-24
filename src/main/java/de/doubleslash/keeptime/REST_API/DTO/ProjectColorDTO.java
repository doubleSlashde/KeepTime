package de.doubleslash.keeptime.REST_API.DTO;

import de.doubleslash.keeptime.REST_API.DTO.ColorDTO;

public class ProjectColorDTO {
   private long id;
   private String name;
   private String description;
   private ColorDTO color;
   private boolean isWork;
   private boolean isDefault;
   private boolean isEnabled;
   private int index;


   public ProjectColorDTO( long id, String name, String description, ColorDTO color, boolean isWork, int index, boolean isDefault) {
      this.id= id;
      this.name = name;
      this.description = description;
      this.color = color;
      this.isWork = isWork;
      this.index = index;
      this.isDefault = isDefault;
      this.isEnabled = true;
   }

   public long getId() {
      return id;
   }

   public void setId(final long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public ColorDTO getColor() {
      return color;
   }

   public void setColor(ColorDTO color) {
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

   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
   }


}
