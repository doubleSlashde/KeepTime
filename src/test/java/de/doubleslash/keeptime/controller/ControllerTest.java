package de.doubleslash.keeptime.controller;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;

public class ControllerTest {

   static Controller testee;

   @BeforeClass
   public static void beforeTest() {
      final Model model = new Model();
      testee = new Controller(model);
   }

   @Test
   public void moveProjectFromEndToStart() {

      final List<Integer> expectedOrderAfter = Arrays.asList(1, 2, 3, 0);

      final Project project0 = new Project();
      project0.setIndex(0);
      final Project project1 = new Project();
      project1.setIndex(1);
      final Project project2 = new Project();
      project2.setIndex(2);

      final Project project3 = new Project();
      final int oldIndex = 3;
      project3.setIndex(oldIndex);
      final int newIndex = 0;
      project3.setIndex(newIndex);

      // change index to 0
      final List<Project> projectList = Arrays.asList(project0, project1, project2, project3);
      testee.resortProjectIndexes(projectList, project3, oldIndex, newIndex);

      for (int i = 0; i < projectList.size(); i++) {
         assertThat(projectList.get(i).getIndex(), Matchers.is(expectedOrderAfter.get(i)));
      }

   }

   @Test
   public void moveProjectFromStartToEnd() {

      final List<Integer> expectedOrderAfter = Arrays.asList(3, 0, 1, 2);

      final Project project0 = new Project();
      final int oldIndex = 0;
      project0.setIndex(oldIndex);
      final int newIndex = 3;
      project0.setIndex(newIndex);

      final Project project1 = new Project();
      project1.setIndex(1);
      final Project project2 = new Project();
      project2.setIndex(2);
      final Project project3 = new Project();
      project3.setIndex(3);

      final List<Project> projectList = Arrays.asList(project0, project1, project2, project3);
      testee.resortProjectIndexes(projectList, project0, oldIndex, newIndex);

      for (int i = 0; i < projectList.size(); i++) {
         assertThat(projectList.get(i).getIndex(), Matchers.is(expectedOrderAfter.get(i)));
      }

   }

   @Test
   public void moveProjectForward() {

      final List<Integer> expectedOrderAfter = Arrays.asList(0, 2, 1, 3);

      final Project project0 = new Project();
      project0.setIndex(0);

      final Project project1 = new Project();
      final int oldIndex = 1;
      project1.setIndex(oldIndex);
      final int newIndex = 2;
      project1.setIndex(newIndex);
      final Project project2 = new Project();
      project2.setIndex(2);
      final Project project3 = new Project();
      project3.setIndex(3);

      final List<Project> projectList = Arrays.asList(project0, project1, project2, project3);
      testee.resortProjectIndexes(projectList, project1, oldIndex, newIndex);

      for (int i = 0; i < projectList.size(); i++) {
         assertThat(projectList.get(i).getIndex(), Matchers.is(expectedOrderAfter.get(i)));
      }

   }

   @Test
   public void moveProjectBackward() {

      final List<Integer> expectedOrderAfter = Arrays.asList(0, 2, 1, 3);

      final Project project0 = new Project();
      project0.setIndex(0);

      final Project project1 = new Project();
      project1.setIndex(1);
      final Project project2 = new Project();
      final int oldIndex = 2;
      project2.setIndex(oldIndex);
      final int newIndex = 1;
      project2.setIndex(newIndex);
      final Project project3 = new Project();
      project3.setIndex(3);

      final List<Project> projectList = Arrays.asList(project0, project1, project2, project3);
      testee.resortProjectIndexes(projectList, project2, oldIndex, newIndex);

      for (int i = 0; i < projectList.size(); i++) {
         assertThat(projectList.get(i).getIndex(), Matchers.is(expectedOrderAfter.get(i)));
      }
   }

   @Test
   public void dontMoveProjectTest() {

      final List<Integer> expectedOrderAfter = Arrays.asList(0, 1, 2, 3);

      final Project project0 = new Project();
      project0.setIndex(0);

      final Project project1 = new Project();
      project1.setIndex(1);
      final Project project2 = new Project();
      project2.setIndex(2);
      final Project project3 = new Project();
      project3.setIndex(3);

      final List<Project> projectList = Arrays.asList(project0, project1, project2, project3);
      for (final Project project : projectList) {
         testee.resortProjectIndexes(projectList, project, project.getIndex(), project.getIndex());
      }

      for (int i = 0; i < projectList.size(); i++) {
         assertThat(projectList.get(i).getIndex(), Matchers.is(expectedOrderAfter.get(i)));
      }
   }

}
