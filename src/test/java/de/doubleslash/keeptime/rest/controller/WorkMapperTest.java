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


package de.doubleslash.keeptime.rest.controller;

import de.doubleslash.keeptime.rest.DTO.ProjectIdentificationDTO;
import de.doubleslash.keeptime.rest.DTO.WorkDTO;
import de.doubleslash.keeptime.rest.mapper.WorkMapper;
import de.doubleslash.keeptime.rest.mapper.WorkMapperImpl;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


class WorkMapperTest {

   WorkMapper workMapper = new WorkMapperImpl();

   @Test
   void workToWorkDTO() {
      // ARRANGE
      final Project project = new Project();
      project.setName("ProjectName");
      project.setColor(Color.BLUE);
      project.setDescription("ProjectDescription");

      final LocalDateTime from = LocalDateTime.now();
      final LocalDateTime to = LocalDateTime.now();
      final Work work = new Work(from, to, project, "Did something");

      // ACT
      final WorkDTO workDTO = workMapper.workToWorkDTO(work);

      // ASSERT
      assertEquals(work.getId() ,workDTO.getId());
      assertEquals(from ,workDTO.getStartTime());
      assertEquals(to ,workDTO.getEndTime());
      assertEquals("Did something", workDTO.getNotes());

      assertEquals(project.getId(), workDTO.getProject().getId());
   }

   @Test
   public void workDTOToWork() {
      // Arrange
      LocalDateTime startTime = LocalDateTime.of(2024, 4, 22, 9, 0);
      LocalDateTime endTime = LocalDateTime.of(2024, 4, 22, 17, 0);
      ProjectIdentificationDTO projectIdentificationDTO = new ProjectIdentificationDTO(0);
      String notes = "Test Notizen";
      WorkDTO workDTO = new WorkDTO(1,startTime, endTime, projectIdentificationDTO, notes);

      // Act
      Work work = workMapper.workDTOToWork(workDTO);

      // Assert
      assertNotNull(work);
      assertEquals(startTime, work.getStartTime());
      assertEquals(endTime, work.getEndTime());
      assertNotNull(work.getProject());
      assertEquals(projectIdentificationDTO.getId(), work.getProject().getId());
      assertEquals(notes, work.getNotes());
   }
}