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

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "Work")
public class Work {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   private LocalDateTime startTime;
   private LocalDateTime endTime;

   @ManyToOne
   private Project project;
   @Lob
   private String notes;

   public Work() {
   }

   public Work(final LocalDateTime startTime, final LocalDateTime endTime, final Project project, final String notes) {
      super();
      this.startTime = startTime;
      this.endTime = endTime;
      this.project = project;
      this.notes = notes;
   }

   public long getId() {
      return id;
   }

   public LocalDateTime getStartTime() {
      return startTime;
   }

   public void setStartTime(final LocalDateTime startTime) {
      this.startTime = startTime;
   }

   public LocalDateTime getEndTime() {
      return endTime;
   }

   public void setEndTime(final LocalDateTime endTime) {
      this.endTime = endTime;
   }

   public Project getProject() {
      return project;
   }

   public void setProject(final Project project) {
      this.project = project;
   }

   public String getNotes() {
      return notes;
   }

   public void setNotes(final String notes) {
      this.notes = notes;
   }

   @Override
   public String toString() {
      return "Work [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", projectName="
            + project.getName() + ", notes=" + notes + "]";
   }

}
