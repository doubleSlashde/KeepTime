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
}