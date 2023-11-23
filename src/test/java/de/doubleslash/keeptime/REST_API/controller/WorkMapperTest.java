package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.scene.paint.Color;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

   @Test
   void workDTOToWork() {
   }
}