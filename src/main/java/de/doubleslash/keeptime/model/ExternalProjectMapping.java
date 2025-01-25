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

import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import jakarta.persistence.*;
import javafx.scene.paint.Color;

@Entity
@Table(name = "ExternalProjectMapping")
public class ExternalProjectMapping {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   // TODO maybe add a externalSystem Identifier

   @Lob
   private String externalProjectData;

   @ManyToOne
   private Project project;

   public ExternalProjectMapping() {
      // Needed for jpa
   }

   public ExternalProjectMapping(final String externalProjectData, final Project project) {
      this.externalProjectData = externalProjectData;
      this.project = project;
   }

   public String getExternalProjectData() {
      return externalProjectData;
   }

   public void setExternalProjectData(final String externalProjectData) {
      this.externalProjectData = externalProjectData;
   }

   public Project getProject() {
      return project;
   }

   public void setProject(final Project project) {
      this.project = project;
   }
}