package de.doubleslash.keeptime.controller;

import de.doubleslash.keeptime.model.ExternalProjectMapping;
import de.doubleslash.keeptime.model.ExternalSystem;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTime;
import javafx.scene.paint.Color;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class HeimatControllerTest {

   private static HeimatAPI mockedHeimatAPI;
   private static ExternalProjectsMappingsRepository mockedExternalMappingsRepository;
   private static HeimatController heimatController;

   final List<ExternalProjectMapping> externalMappings = new ArrayList<>();

   final ArrayList<Work> workItems = new ArrayList<>();
   final LocalDateTime now = LocalDateTime.now();
   final List<HeimatTask> availableTasks = new ArrayList<>();

   final Project workProject1 = new Project("Project 1", "", Color.RED, true, 0);
   final Project workProject2 = new Project("Project 2", "", Color.RED, true, 1);

   final ExternalProjectMapping project1To1Mapping = new ExternalProjectMapping(ExternalSystem.Heimat,
         "External Project 1", 1L, "External Task 1", "", workProject1);
   final ExternalProjectMapping project2To1Mapping = new ExternalProjectMapping(ExternalSystem.Heimat,
         "External Project 1", 1L, "External Task 1", "", workProject2);

   @BeforeEach
   public void beforeEach() {
      mockedHeimatAPI = Mockito.mock(HeimatAPI.class);
      mockedExternalMappingsRepository = Mockito.mock(ExternalProjectsMappingsRepository.class);
      heimatController = new HeimatController(mockedHeimatAPI, mockedExternalMappingsRepository,
            new Controller(null, null, null, null));

      when(mockedExternalMappingsRepository.findByExternalSystemId(ExternalSystem.Heimat)).thenReturn(externalMappings);

      availableTasks.add(
            new HeimatTask(project1To1Mapping.getExternalTaskId(), project1To1Mapping.getExternalTaskName(),
                  project1To1Mapping.getExternalProjectName(), false, "", false, false));
      when(mockedHeimatAPI.getMyTasks(now.toLocalDate())).thenReturn(availableTasks);
   }

   @Test
   void shouldMarkNonSyncableWhenNotMapped() {
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));

      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      assertFalse(tableRows.get(0).canBeSynced());
   }

   @Test
   void shouldMarkNonSyncableWhenHeimatTaskNoLongerExists() {
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));
      externalMappings.add(project1To1Mapping);
      availableTasks.clear();

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);

      // ASSERT
      assertFalse(tableRows.get(0).canBeSynced());
      assertThat(tableRows.get(0).syncMessage(), Matchers.containsString("no longer available"));
   }

   @Test
   void shouldBeSyncableWhenMapped() {
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));
      externalMappings.add(project1To1Mapping);

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ASSERT
      assertTrue(mapping.canBeSynced());
      assertThat(mapping.keeptimeSeconds(), Matchers.is(13 * 60L));
      assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1"));
      assertThat(mapping.syncMessage(), Matchers.containsString(project1To1Mapping.getExternalTaskName()));
   }

   @Test
   void shouldAddHeimatDetailsWhenAlreadyPresent() {
      // ARRANGE
      final Work work1 = new Work(now.minusMinutes(13), now, workProject1, "Notes 1");
      workItems.add(work1);
      externalMappings.add(project1To1Mapping);
      final HeimatTime existingTime1 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 60, "Existing note 1", 12);
      // there could be more than 1 time for a task in heimat (e.g. when manually saved with start,end feature)
      final HeimatTime existingTime2 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 30, "Existing note 2", 13);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime1, existingTime2));

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ASSERT
      assertAll(() -> assertTrue(mapping.canBeSynced()),
            () -> assertThat(mapping.keeptimeSeconds(), Matchers.is(13 * 60L)),
            () -> assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1")),
            () -> assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1)),
            () -> assertThat(mapping.heimatNotes(), Matchers.is("Existing note 1. Existing note 2")),
            () -> assertThat(mapping.heimatSeconds(), Matchers.is((60 + 30) * 60L)),
            () -> assertThat(mapping.existingTimes(), Matchers.containsInAnyOrder(existingTime1, existingTime2))
            //
      );
   }

   @Test
   void shouldCombineAllWorksAndProjectsWhenMultipleProjectsAreMappedToSame() {
      // ARRANGE
      final Work work1 = new Work(now.minusMinutes(13), now, workProject1, "Notes 1");
      workItems.add(work1);
      final Work work2 = new Work(now.minusMinutes(13), now, workProject2, "Notes 2");
      workItems.add(work2);
      externalMappings.add(project1To1Mapping);
      externalMappings.add(project2To1Mapping);

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ASSERT
      assertAll(() -> assertTrue(mapping.canBeSynced()),
            () -> assertThat(mapping.keeptimeSeconds(), Matchers.is(2 * 13 * 60L)),
            () -> assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1. Notes 2")),
            () -> assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1, workProject2)));
   }

   // shouldShowHeimatTimeWhenProjectIsNotMappedInKeeptime? (to know absolute time for the day)

   // Save
   // shouldSaveTimes
   // shouldDeleteExistingTimesBeforeSavingWhenTimesAlreadyExist
   // shouldContinueOnErrorAndReturnErrorsWhenErrorsOccurred
   // shouldOnlyUpdateHeimatWhenSomethingHasChanged

}