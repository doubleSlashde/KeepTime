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


package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.REST_API.DTO.ColorDTO;
import de.doubleslash.keeptime.REST_API.DTO.ProjectDTO;
import de.doubleslash.keeptime.REST_API.DTO.WorkDTO;
import de.doubleslash.keeptime.REST_API.mapper.WorkMapper;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


class WorkMapperTest {

   WorkMapper workMapper = WorkMapper.INSTANCE;

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
      assertEquals("Did something", workDTO.getNotes());
      assertEquals(5,workDTO.getId());
      //... assert other fields

      assertEquals(0, workDTO.getProject().getId());
   }

//   @Test
//   void WorkDTOToWork() {
//      // ARRANGE
//      LocalDateTime startTime = LocalDateTime.of(2024, 4, 19, 9, 0);
//      LocalDateTime endTime = LocalDateTime.of(2024, 4, 19, 17, 0);
//
//      // Erstellen eines ColorDTO-Objekts für das Projekt
//      ColorDTO colorDTO = new ColorDTO();
//      colorDTO.setRed(0.0);
//      colorDTO.setGreen(0.0);
//      colorDTO.setBlue(1.0);
//
//      // Erstellen eines ProjectDTO
//      ProjectDTO projectDTO = new ProjectDTO(1);
//      projectDTO.setId(1);
//
//
//      // Erstellen einer WorkDTO
//      WorkDTO workDTO = new WorkDTO(1, startTime, endTime, projectDTO, "Did something");
//
//      // ACT
//      // Konvertieren der WorkDTO in ein Work-Objekt
//      Work work = workMapper.workDTOToWork(workDTO);
//
//      // ASSERT
//      // Überprüfen der notwendigen Felder des erstellten Work-Objekts
//      assertEquals(1, work.getId());
//
//   }


   @Test
   public void testWorkDTOToWork() {
      // Arrange
      LocalDateTime startTime = LocalDateTime.of(2024, 4, 22, 9, 0); // Beispielzeit
      LocalDateTime endTime = LocalDateTime.of(2024, 4, 22, 17, 0); // Beispielzeit
      ProjectDTO projectDTO = new ProjectDTO(0);
      String notes = "Test Notizen";
      WorkDTO workDTO = new WorkDTO(1,startTime, endTime, projectDTO, notes);

      // Act
      Work work = workMapper.workDTOToWork(workDTO);

      // Assert
      assertNotNull(work);
      assertEquals(startTime, work.getStartTime());
      assertEquals(endTime, work.getEndTime());
      assertNotNull(work.getProject());
      assertEquals(projectDTO.getId(), work.getProject().getId());
      assertEquals(notes, work.getNotes());
   }
}