package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.REST_API.ProjectColorDTO;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {
   ProjectMapper projectMapper= ProjectMapper.INSTANCE;

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

//   @Test
//   void projectDTOToProject() {
//      //ARRANGE
//      ProjectColorDTO project = new ProjectColorDTO();
//      project.setName("ProjectName");
//      project.setDescription("ProjectDescription");
//      project.setColor();
//      project.setIndex(0);
//      project.setDefault(true);
//      project.setEnabled(true);
//      project.setWork(false);
//
////ColorDTO colorDTO;
//
//      //ACT
//      final ProjectColorDTO projectColorDTO = new ProjectColorDTO("ddssdf","Test", colorDTO.getRed(),colorDTO.getBlue(),true,2,true);
//            Project project1= projectMapper.projectDTOToProject(projectColorDTO);
//      //Assert
//      assertEquals(Color.BLUE.getBlue(), projectColorDTO.getColor().getBlue());
//
//   }
}
