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

package de.doubleslash.keeptime.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import de.doubleslash.keeptime.common.DateProvider;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.SettingsRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import javafx.scene.paint.Color;

class ControllerTest {

   private static Controller testee;
   private  Model model;
   private  DateProvider mockedDateProvider;

   private WorkRepository mockedWorkRepository;

   @BeforeEach
   void beforeTest() {
      mockedWorkRepository = Mockito.mock(WorkRepository.class);
      model = new Model(Mockito.mock(ProjectRepository.class), mockedWorkRepository,
            Mockito.mock(SettingsRepository.class));
      mockedDateProvider = Mockito.mock(DateProvider.class);
      testee = new Controller(model, mockedDateProvider);
   }

   @Test
   void moveProjectFromEndToStart() {

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
   void moveProjectFromStartToEnd() {

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
   void moveProjectForward() {

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
   void moveProjectBackward() {

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
         assertThat(projectList.get(i).getIndex(), is(expectedOrderAfter.get(i)));
      }
   }

   @Test
   void dontMoveProjectTest() {

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
         assertThat(projectList.get(i).getIndex(), is(expectedOrderAfter.get(i)));
      }
   }

   @Test
   void changeProjectSameDayTest() {
      final LocalDateTime firstProjectDateTime = LocalDateTime.now();
      final LocalDateTime secondProjectDateTime = LocalDateTime.now();

      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(firstProjectDateTime);
      final Project firstProject = new Project("1st Project", "A good description", Color.GREEN, true, 0);
      final Project secondProject = new Project("2nd Project", "An even better description", Color.RED, true, 1);
      testee.changeProject(firstProject);
      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(secondProjectDateTime);
      testee.changeProject(secondProject);

      Mockito.verify(model.getWorkRepository(), Mockito.times(1)).save(Mockito.argThat((final Work savedWork) -> {
         if (savedWork.getProject() != firstProject) {
            return false;
         }
         if (!savedWork.getStartTime().equals(firstProjectDateTime)) {
            return false;
         }
         if (!savedWork.getEndTime().equals(secondProjectDateTime)) {
            return false;
         }
         return true;
      }));
      assertThat("Two project should be in the past work items", model.getPastWorkItems().size(), is(2));
      assertThat("The first project should be the past work project", model.getPastWorkItems().get(0).getProject(),
            is(firstProject));
      assertThat("The second project should be the active work project", model.activeWorkItem.get().getProject(),
            is(secondProject));
   }

   @Test
   void changeProjectOtherDayTest() {
      Mockito.when(mockedWorkRepository.save(Mockito.any(Work.class))).thenAnswer(i -> i.getArguments()[0]);
      final LocalDateTime firstProjectDateTime = LocalDateTime.now();
      final LocalDateTime secondProjectDateTime = firstProjectDateTime.plusDays(1); // project is create the next day

      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(firstProjectDateTime);
      final Project firstProject = new Project("1st Project", "A good description", Color.GREEN, true, 0);
      final Project secondProject = new Project("2nd Project", "An even better description", Color.RED, true, 1);
      testee.changeProject(firstProject);
      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(secondProjectDateTime);
      testee.changeProject(secondProject);

      Mockito.verify(model.getWorkRepository(), Mockito.times(1)).save(Mockito.argThat((final Work savedWork) -> {
         if (savedWork.getProject() != firstProject) {
            return false;
         }
         if (!savedWork.getStartTime().equals(firstProjectDateTime)) {
            return false;
         }
         if (!savedWork.getEndTime().equals(secondProjectDateTime)) {
            return false;
         }
         return true;
      }));
      assertThat("'2nd project' should be in the past work items", model.getPastWorkItems().size(), is(1));
      assertThat("The project should be  '2ndProject'", model.getPastWorkItems().get(0).getProject(),
            is(secondProject));
      assertThat("'2ndProject' should be the active work project", model.activeWorkItem.get().getProject(),
            is(secondProject));
   }

   @Test
   void changeProjectOtherDayWithTimeTest() {
      final LocalDateTime firstProjectDateTime = LocalDateTime.of(2018, 2, 14, 14, 0);
      final LocalDateTime secondProjectDateTime = firstProjectDateTime.plusDays(1); // project is create the next day

      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(firstProjectDateTime);
      final Project firstProject = new Project("1st Project", "A good description", Color.GREEN, true, 0);
      final Project secondProject = new Project("2nd Project", "An even better description", Color.RED, true, 1);
      testee.changeProject(firstProject);
      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(secondProjectDateTime);
      testee.changeProject(secondProject, 23 * 60 * 60); // change with -23 hours

      final LocalDateTime firstProjectPlusOneHour = firstProjectDateTime.plusHours(1);
      Mockito.verify(model.getWorkRepository(), Mockito.times(1)).save(Mockito.argThat((final Work savedWork) -> {
         if (savedWork.getProject() != firstProject) {
            return false;
         }
         if (!savedWork.getStartTime().equals(firstProjectDateTime)) {
            return false;
         }
         if (!savedWork.getEndTime().equals(firstProjectPlusOneHour)) {
            return false;
         }
         return true;
      }));

      assertThat("Two projects should be in the past work items", model.getPastWorkItems().size(), is(2));
      assertThat("The first project should be '1st Project'", model.getPastWorkItems().get(0).getProject(),
            is(firstProject));
      assertThat("The second project should be '2ndProject'", model.getPastWorkItems().get(1).getProject(),
            is(secondProject));
      final Work work = model.activeWorkItem.get();
      assertThat("'2ndProject' should be the active work project", work.getProject(), is(secondProject));
      assertThat(work.getStartTime().toLocalDate(), is(firstProjectDateTime.toLocalDate()));
      assertThat(work.getStartTime(), is(firstProjectPlusOneHour));
   }

   @Test
   void shouldCalculateSecondsCorrectlyWhenWorkItemsAreGiven() {
      final Project workProject1 = new Project("workProject1", "Some description", Color.GREEN, true, 0);
      final Project workProject2 = new Project("workProject2", "A good description", Color.RED, true, 1);
      final Project nonworkProject1 = new Project("nonworkProject1", "An even better description", Color.RED, false, 2);
      final Project nonworkProject2 = new Project("nonworkProject2", "The best description", Color.RED, false, 3);

      final LocalDateTime localDateTimeMorning = LocalDateTime.now().withHour(4);

      final List<Work> workItems = new ArrayList<>(4);
      workItems.add(new Work(localDateTimeMorning.plusHours(0), localDateTimeMorning.plusHours(1), workProject1, ""));
      workItems.add(new Work(localDateTimeMorning.plusHours(1), localDateTimeMorning.plusHours(2), workProject2, ""));
      workItems
            .add(new Work(localDateTimeMorning.plusHours(2), localDateTimeMorning.plusHours(3), nonworkProject1, ""));
      workItems
            .add(new Work(localDateTimeMorning.plusHours(3), localDateTimeMorning.plusHours(4), nonworkProject2, ""));

      model.getAllProjects().addAll(workProject1, workProject2, nonworkProject1, nonworkProject2);
      model.getPastWorkItems().addAll(workItems);

      final long totalSeconds = testee.calcSeconds(workItems);
      assertEquals("Seconds were not calculated correctly.", TimeUnit.HOURS.toSeconds(4), totalSeconds);

      final long todaysSeconds = testee.calcTodaysSeconds();
      assertEquals("Todays seconds were not calulated correctly.", TimeUnit.HOURS.toSeconds(4), todaysSeconds);

      // final long todaysWorkSeconds = testee.calcTodaysWorkSeconds();
      // assertEquals("Todays work seconds were not calculated correctly.",TimeUnit.HOURS.toSeconds(2),
      // todaysWorkSeconds);
      // TODO does not work, as id within project cannot be set

   }

   @Test
   void shouldUpdateWorkItemPersistentlyWhenWorkItemIsEdited() {
      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());
      Mockito.when(mockedWorkRepository.save(Mockito.any(Work.class)))
            .thenAnswer(invocation -> invocation.getArguments()[0]);

      final Project project1 = new Project("workProject1", "Some description", Color.RED, true, 0);
      model.getAllProjects().add(project1);

      final LocalDateTime localDateTimeMorning = LocalDateTime.now().withHour(4);

      final Work originalWork = new Work(localDateTimeMorning.plusHours(0), localDateTimeMorning.plusHours(1), project1,
            "originalWork");
      model.getPastWorkItems().add(originalWork);

      final Work newWork = new Work(localDateTimeMorning.plusHours(1), localDateTimeMorning.plusHours(2), project1,
            "updated");

      testee.editWork(originalWork, newWork);

      final Work testWork = model.getPastWorkItems().get(0);
      assertThat("Start time was not updated", testWork.getStartTime(), equalTo(newWork.getStartTime()));
      assertThat("End timewas not updated", testWork.getEndTime(), equalTo(newWork.getEndTime()));
      assertThat("Notes were not updated", testWork.getNotes(), equalTo(newWork.getNotes()));
      assertThat("Project was not updated", testWork.getProject(), equalTo(newWork.getProject()));

      final ArgumentCaptor<Work> argument = ArgumentCaptor.forClass(Work.class);
      Mockito.verify(mockedWorkRepository, Mockito.times(1)).save(argument.capture());
      assertThat("Edited work was not saved persistently", argument.getValue(), is(originalWork));

   }

   @Test
   void shouldNotUpdateOthersWhenWorkItemIsEdited() {
      Mockito.when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());
      Mockito.when(mockedWorkRepository.save(Mockito.any(Work.class)))
            .thenAnswer(invocation -> invocation.getArguments()[0]);

      final Project project1 = new Project("workProject1", "Some description", Color.RED, true, 0);
      model.getAllProjects().add(project1);

      final LocalDateTime localDateTimeMorning = LocalDateTime.now().withHour(4);

      final Work notToBeUpdatedWork = new Work(localDateTimeMorning.plusHours(0), localDateTimeMorning.plusHours(1),
            project1, "originalWork");
      ReflectionTestUtils.setField(notToBeUpdatedWork, "id", 1);
      model.getPastWorkItems().add(notToBeUpdatedWork);

      final Work originalWork = new Work(localDateTimeMorning.plusHours(1), localDateTimeMorning.plusHours(2), project1,
            "originalWork");
      ReflectionTestUtils.setField(originalWork, "id", 2);
      model.getPastWorkItems().add(originalWork);

      final Work newWork = new Work(localDateTimeMorning.plusHours(3), localDateTimeMorning.plusHours(4), project1,
            "updated");
      ReflectionTestUtils.setField(newWork, "id", 3);

      testee.editWork(originalWork, newWork);

      assertThat("Too many or too less work items in past work items", model.getPastWorkItems().size(), equalTo(2));
      assertThat("Work with new values in past work items instead of updatd work", model.getPastWorkItems(),
            not(contains(newWork)));

      final ArgumentCaptor<Work> argument = ArgumentCaptor.forClass(Work.class);
      Mockito.verify(mockedWorkRepository, Mockito.times(1)).save(argument.capture());
      assertThat("Saved other Work persistently than what should be edited", argument.getValue(),
            not(is(notToBeUpdatedWork)));

   }

   @Test
   void shouldDeleteWorkPersistentlyWhenWorkIsDeleted() {

      final Project project1 = new Project("workProject1", "Some description", Color.RED, true, 0);
      model.getAllProjects().add(project1);

      final LocalDateTime localDateTimeMorning = LocalDateTime.now().withHour(4);

      final Work work = new Work(localDateTimeMorning.plusHours(0), localDateTimeMorning.plusHours(1), project1,
            "originalWork");
      model.getPastWorkItems().add(work);

      testee.deleteWork(work);

      final ArgumentCaptor<Work> argument = ArgumentCaptor.forClass(Work.class);
      Mockito.verify(mockedWorkRepository, Mockito.times(1)).delete(argument.capture());
      assertThat("Edited work was not deleted persistently", argument.getValue(), is(work));

   }

   @Test
   void shouldRemoveWorkFromPastWorkItemsWhenWorkIsDeleted() {

      final Project project1 = new Project("workProject1", "Some description", Color.RED, true, 0);
      model.getAllProjects().add(project1);

      final LocalDateTime localDateTimeMorning = LocalDateTime.now().withHour(4);

      final Work work = new Work(localDateTimeMorning.plusHours(0), localDateTimeMorning.plusHours(1), project1,
            "originalWork");
      model.getPastWorkItems().add(work);

      testee.deleteWork(work);

      assertTrue("work Items contain work when it should have been deleted", model.getPastWorkItems().isEmpty());

   }

}
