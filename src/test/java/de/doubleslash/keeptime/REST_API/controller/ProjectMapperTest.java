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
