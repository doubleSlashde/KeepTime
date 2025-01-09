// Copyright 2019 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.model;

import jakarta.persistence.*;

import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import javafx.scene.paint.Color;

@Entity
@Table(name = "Project")
public class Project {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   private String name;

   @Lob
   private String description;

   @Convert(converter = ColorConverter.class)
   private Color color;

   private boolean isWork;

   private boolean isDefault;

   private boolean isEnabled;

   private int index;

   public Project() {
      // Needed for jpa
   }

   public Project(final String name, final String description, final Color color, final boolean isWork, final int index,
         final boolean isDefault) {
      super();
      this.name = name;
      this.description = description;
      this.color = color;
      this.isWork = isWork;
      this.isDefault = isDefault;
      this.isEnabled = true;
      this.index = index;
   }

   public Project(final String name, final String description, final Color color, final boolean isWork,
         final int index) {
      this(name, description, color, isWork, index, false);
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

   public int getIndex() {
      return index;
   }

   public void setIndex(final int index) {
      this.index = index;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   @Override
   public String toString() {
      return "Project [id=" + id + ", name=" + name + ", description=" + description + ", color=" + color + ", isWork="
            + isWork + ", isDefault=" + isDefault + ", isEnabled=" + isEnabled + ", index=" + index + "]";
   }

}
