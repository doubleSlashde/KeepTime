package de.doubleslash.keeptime.controller.report;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.DateProvider;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.SettingsRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
public class CalculateReportTest {

   private static Controller controller;
   private Model model;
   private DateProvider mockedDateProvider;
   private WorkRepository mockedWorkRepository;

   @BeforeEach
   void beforeTest() {
      mockedWorkRepository = Mockito.mock(WorkRepository.class);
      mockedDateProvider = Mockito.mock(DateProvider.class);
      model = new Model(Mockito.mock(ProjectRepository.class), mockedWorkRepository,
            Mockito.mock(SettingsRepository.class));
      controller = new Controller(model, mockedDateProvider);
   }

   @Test
   void whenGetReportStructureThenReturnRightDate() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate();
      String expected = DateFormatter.toDayDateString(date);

      // ACT
      Report report = new Report(date, model, controller);

      // ASSERT
      assertThat(report.getDateString(), is(expected));
   }

   @Test
   void whenNoTimesThenReturnBaseReport() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate();
      String expectedTime = "00:00:00";
      List<Work> emptyWorkList = new ArrayList<>();
      final SortedSet<Project> emptyProjectSet = Collections.emptySortedSet();
      Long expectedWorkSeconds = 0L;

      when(model.getWorkRepository().findByStartDateOrderByStartTimeAsc(any())).thenReturn(emptyWorkList);

      // ACT
      Report report = new Report(date, model, controller);


      // ASSERT
      assertThat(report.getWorkTimeString(), is(expectedTime));
      assertThat(report.getPresentTimeString(), is(expectedTime));
      assertThat(report.getWorkItems(), is(emptyWorkList));
      assertThat(report.getWorkItemsSeconds(), is(expectedWorkSeconds));
      assertThat(report.getWorkedProjectsSet(), is(emptyProjectSet));
   }

   @Test
   void whenOneWorkItemWithNoWorkTime() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate();
      long presentTime = 60L;
      long workTime = 0L;
      String expectedPresentTime = DateFormatter.secondsToHHMMSS(presentTime);
      String expectedWorkTime = DateFormatter.secondsToHHMMSS(workTime);

      List<Work> workList = new ArrayList<>();
      Project project = new Project("Idle", "description", Color.ORANGE, false, 0, true);
      LocalDateTime startTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 0);
      LocalDateTime endTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 1, 0);
      workList.add(new Work(startTime, endTime, project, "notes"));

      final SortedSet<Project> projects = workList.stream()
                      .map(Work::getProject)
                      .collect(Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(Project::getIndex))));

      when(model.getWorkRepository().findByStartDateOrderByStartTimeAsc(any())).thenReturn(workList);

      // ACT
      Report report = new Report(date, model, controller);

      // ASSERT
      assertThat(report.getPresentTimeString(), is(expectedPresentTime));
      assertThat(report.getWorkTimeString(), is(expectedWorkTime));
      assertThat(report.getWorkedProjectsSet(), is(projects));
      assertThat(report.getWorkItems(), is(workList));
   }

   @Test
   void whenTwoWorkItemsWithWorkAndPresentTime() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate();
      long presentTime = 90L;
      long workTime = 60L;
      String expectedPresentTime = DateFormatter.secondsToHHMMSS(presentTime);
      String expectedWorkTime = DateFormatter.secondsToHHMMSS(workTime);

      List<Work> workList = new ArrayList<>();
      Project presentProject = new Project("Idle", "description", Color.ORANGE, false, 0, true);
      Project workProject = new Project("Project", "description", Color.BLACK, true, 1);

      LocalDateTime startTimePresent = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 0);
      LocalDateTime endTimePresent = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 30);
      LocalDateTime startTimeWork = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 30);
      LocalDateTime endTimeWork = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 1, 30);

      workList.add(new Work(startTimePresent, endTimePresent, presentProject, "notes"));
      workList.add(new Work(startTimeWork, endTimeWork, workProject, "notes"));

      final SortedSet<Project> projects = workList.stream()
                                                  .map(Work::getProject)
                                                  .collect(Collectors.toCollection(() -> new TreeSet<>(
                                                        Comparator.comparing(Project::getIndex))));

      when(model.getWorkRepository().findByStartDateOrderByStartTimeAsc(any())).thenReturn(workList);

      // ACT
      Report report = new Report(date, model, controller);

      // ASSERT
      assertThat(report.getPresentTimeString(), is(expectedPresentTime));
      assertThat(report.getWorkTimeString(), is(expectedWorkTime));
      assertThat(report.getWorkedProjectsSet(), is(projects));
      assertThat(report.getWorkItems(), is(workList));
   }

   @Test
   void whenWorkItemsWithWorkThenReturnRightTimes() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate();

      List<Work> workList = new ArrayList<>();
      Project presentProject = new Project("Idle", "description", Color.ORANGE, false, 0, true);
      Project workProject = new Project("Project", "description", Color.BLACK, true, 1);

      LocalDateTime startTimePresent = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 0);
      LocalDateTime endTimePresent = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 30);
      LocalDateTime startTimeWork = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 0, 30);
      LocalDateTime endTimeWork = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 8, 1, 30);

      workList.add(new Work(startTimePresent, endTimePresent, presentProject, "notes"));
      workList.add(new Work(startTimeWork, endTimeWork, workProject, "notes"));

      final SortedSet<Project> projects = workList.stream()
                                                  .map(Work::getProject)
                                                  .collect(Collectors.toCollection(() -> new TreeSet<>(
                                                        Comparator.comparing(Project::getIndex))));

      Map<Long, Long> map = new HashMap<>();
      for (final Project project : projects) {
         final List<Work> onlyCurrentProjectWork = workList.stream()
                                                           .filter(w -> w.getProject() == project)
                                                           .collect(Collectors.toList());

         final long projectWorkSeconds = controller.calcSeconds(onlyCurrentProjectWork);
         map.put(project.getId(), projectWorkSeconds);
      }

      when(model.getWorkRepository().findByStartDateOrderByStartTimeAsc(any())).thenReturn(workList);

      // ACT
      Report report = new Report(date, model, controller);

      // ASSERT
      assertThat(report.getProjectWorkSecondsMap(), is(map));
   }

   @Test
   void whenDayIsTodayThenShowActiveWork() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate();

      LocalDateTime now = mockedDateProvider.dateTimeNow();
      LocalDateTime startTime = mockedDateProvider.dateTimeNow().minusSeconds(120);
      LocalDateTime endTime = mockedDateProvider.dateTimeNow().minusSeconds(10);

      List<Work> workList = new ArrayList<>();
      Project presentProject = new Project("Idle", "description", Color.ORANGE, false, 0, true);
      workList.add(new Work(startTime, endTime, presentProject, "notes"));

      Project newProject = new Project("project", "description", Color.WHITE, true, 1);
      Work unsavedWork = new Work(endTime, endTime, newProject, "");
      model.activeWorkItem.set(unsavedWork);

      // ACT
      Report report = new Report(date, model, controller);

      // ASSERT
      assertThat(report.getWorkItems(), contains(unsavedWork));
      assertThat(report.getWorkedProjectsSet(), contains(newProject));
   }

   @Test
   void whenDayIsNotTodayThenDoNotShowActiveWork() {
      // ARRANGE
      when(mockedDateProvider.dateTimeNow()).thenReturn(LocalDateTime.now());

      LocalDate date = mockedDateProvider.dateTimeNow().toLocalDate().minusDays(1);

      LocalDateTime now = mockedDateProvider.dateTimeNow();
      LocalDateTime startTime = mockedDateProvider.dateTimeNow().minusDays(1).minusSeconds(120);
      LocalDateTime endTime = mockedDateProvider.dateTimeNow().minusDays(1).minusSeconds(10);

      List<Work> workList = new ArrayList<>();
      Project presentProject = new Project("Idle", "description", Color.ORANGE, false, 0, true);
      workList.add(new Work(startTime, endTime, presentProject, "notes"));

      Project newProject = new Project("project", "description", Color.WHITE, true, 1);
      Work unsavedWork = new Work(now.minusSeconds(10), now.minusSeconds(10), newProject, "");
      model.activeWorkItem.set(unsavedWork);

      when(model.getWorkRepository().findByStartDateOrderByStartTimeAsc(any())).thenReturn(workList);

      // ACT
      Report report = new Report(date, model, controller);

      // ASSERT
      assertThat(report.getWorkItems(), not(contains(unsavedWork)));
      assertThat(report.getWorkedProjectsSet(), not(contains(newProject)));
   }
}
