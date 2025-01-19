// Copyright 2024 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.rest.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

public class ProjectDTO {
   private long id;
   @NotEmpty(message = "Name must not be null or empty")
   private String name;
   private String description;
   /**
    * Color in format of 0xRRGGBBAA (R=Red, G=Green, B=Blue, A=Alpha). E.g. 0xff0000ff is fully opaque red.
    */
   private String color;
   private boolean isWork;
   @PositiveOrZero(message = "Index must not be negative")
   private int index;
   private boolean isEnabled;

   public ProjectDTO( long id, String name, String description, String color, boolean isWork, int index, boolean isEnabled) {
      this.id= id;
      this.name = name;
      this.description = description;
      this.color = color;
      this.isWork = isWork;
      this.index = index;
      this.isEnabled = isEnabled;
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

   public String getColor() {
      return color;
   }

   public void setColor(String color) {
      this.color = color;
   }

   public boolean isWork() {
      return isWork;
   }

   public void setWork(boolean isWork) {
      this.isWork = isWork;
   }

   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public boolean isEnabled() {
      return isEnabled;
   }

   public void setEnabled(final boolean enabled) {
      isEnabled = enabled;
   }
}
