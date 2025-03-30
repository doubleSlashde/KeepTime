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

@Entity
@Table(name = "ExternalProjectMapping")
public class ExternalProjectMapping {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   @Enumerated(EnumType.STRING)
   private ExternalSystem externalSystemId;

   private String externalProjectName;

   private long externalTaskId;
   private String externalTaskName;
   @Lob
   private String externalTaskMetadata;

   @ManyToOne
   private Project project;

   public ExternalProjectMapping() {
      // Needed for jpa
   }

   public ExternalProjectMapping(ExternalSystem externalSystemId, String externalProjectName,
       long externalTaskId, String externalTaskName, String externalTaskMetadata,
       Project project) {
      this.externalSystemId = externalSystemId;
      this.externalProjectName = externalProjectName;
      this.externalTaskId = externalTaskId;
      this.externalTaskName = externalTaskName;
      this.externalTaskMetadata = externalTaskMetadata;
      this.project = project;
   }

   public ExternalSystem getExternalSystemId() {
      return externalSystemId;
   }

   public void setExternalSystemId(ExternalSystem externalSystemId) {
      this.externalSystemId = externalSystemId;
   }

   public String getExternalProjectName() {
      return externalProjectName;
   }

   public void setExternalProjectName(String externalProjectName) {
      this.externalProjectName = externalProjectName;
   }

   public long getExternalTaskId() {
      return externalTaskId;
   }

   public void setExternalTaskId(long externalTaskId) {
      this.externalTaskId = externalTaskId;
   }

   public String getExternalTaskName() {
      return externalTaskName;
   }

   public void setExternalTaskName(String externalTaskName) {
      this.externalTaskName = externalTaskName;
   }

   public String getExternalTaskMetadata() {
      return externalTaskMetadata;
   }

   public void setExternalTaskMetadata(String externalTaskMetadata) {
      this.externalTaskMetadata = externalTaskMetadata;
   }

   public Project getProject() {
      return project;
   }

   public void setProject(Project project) {
      this.project = project;
   }

   @Override
   public String toString() {
      return "ExternalProjectMapping{" + "id=" + id + ", project.name=" + project.getName() + ", externalTaskName='" + externalTaskName
            + '\'' + '}';
   }
}