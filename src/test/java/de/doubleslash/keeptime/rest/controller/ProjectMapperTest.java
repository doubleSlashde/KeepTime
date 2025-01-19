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

import de.doubleslash.keeptime.rest.DTO.ProjectDTO;
import de.doubleslash.keeptime.rest.mapper.ProjectMapper;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ProjectMapperTest {

   @Autowired
   ProjectMapper projectMapper;

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
      final ProjectDTO projectDTO = projectMapper.projectToProjectDTO(project);
      //Assert
      assertEquals("0x0000ffff", projectDTO.getColor());

   }

   @Test
   void projectDTOToProject() {
      // ARRANGE
      ProjectDTO project = new ProjectDTO(1, "ProjectName", "ProjectDescription", "0xff0000ff", false, 0, true);

      // ACT
      final Project project1 = projectMapper.projectDTOToProject(project);

      // ASSERT
      assertEquals(Color.RED, project1.getColor());
      assertEquals("ProjectName", project1.getName());
      assertEquals("ProjectDescription", project1.getDescription());
      assertTrue(project1.isEnabled());
   }
}
