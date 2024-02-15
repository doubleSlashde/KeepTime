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
import de.doubleslash.keeptime.REST_API.DTO.ProjectColorDTO;
import de.doubleslash.keeptime.REST_API.mapper.ProjectMapper;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {
   ProjectMapper projectMapper = ProjectMapper.INSTANCE;

   @Test
   void projectToProjectDTO() {
      //ARRANGE
      Project project = new Project();
      project.setName("ProjectName");
      project.setDescription("ProjectDescription");
      project.setColor(Color.BLUE);
      project.setIndex(0);
      project.setDefault(true);
      project.setEnabled(true);
      project.setWork(false);

      //ACT
      final ProjectColorDTO projectColorDTO = projectMapper.projectToProjectDTO(project);
      //Assert
      assertEquals(Color.BLUE.getBlue(), projectColorDTO.getColor().getBlue());

   }

   @Test
   void projectDTOToProject() {
      // ARRANGE
      ColorDTO colorDTO = new ColorDTO();
      colorDTO.setRed(1.0);
      colorDTO.setGreen(0.0);
      colorDTO.setBlue(0.0);

      ProjectColorDTO project = new ProjectColorDTO(1, "ProjectName", "ProjectDescription", colorDTO, false, 0, true);

      // ACT
      final Project project1 = projectMapper.projectDTOToProject(project);

      // ASSERT
      assertEquals(colorDTO.getBlue(), project1.getColor().getBlue());
      assertEquals("ProjectName", project1.getName());
      assertEquals("ProjectDescription", project1.getDescription());
   }
}
