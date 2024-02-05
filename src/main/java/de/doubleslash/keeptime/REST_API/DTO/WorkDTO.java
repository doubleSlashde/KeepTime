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

package de.doubleslash.keeptime.REST_API.DTO;

import java.time.LocalDateTime;

public class WorkDTO {
   private long id;
   private LocalDateTime startTime;
   private LocalDateTime endTime;
   private ProjectDTO project;
   private String notes;

   public WorkDTO(long id, LocalDateTime startTime, LocalDateTime endTime, ProjectDTO project, String notes) {
      this.id = id;
      this.startTime = startTime;
      this.endTime = endTime;
      this.project = project;
      this.notes = notes;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public LocalDateTime getStartTime() {
      return startTime;
   }

   public void setStartTime(LocalDateTime startTime) {
      this.startTime = startTime;
   }

   public LocalDateTime getEndTime() {
      return endTime;
   }

   public void setEndTime(LocalDateTime endTime) {
      this.endTime = endTime;
   }

   public ProjectDTO getProject() {
      return project;
   }

   public void setProject(ProjectDTO project) {
      this.project = project;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }
}
