// Copyright 2025 doubleSlash Net Business GmbH
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

import de.doubleslash.keeptime.model.ExternalProjectMapping;
import de.doubleslash.keeptime.model.ExternalSystem;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.model.settings.HeimatSettings;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTime;
import javafx.scene.paint.Color;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeimatControllerTest {

   private static HeimatSettings mockedHeimatSettings;
   private static HeimatAPI mockedHeimatAPI;
   private static ExternalProjectsMappingsRepository mockedExternalMappingsRepository;
   private static HeimatController heimatController;

   final List<ExternalProjectMapping> externalMappings = new ArrayList<>();

   final ArrayList<Work> workItems = new ArrayList<>();
   final LocalDateTime now = LocalDateTime.now();
   final List<HeimatTask> availableTasks = new ArrayList<>();

   final Project workProject1 = new Project("Project 1", "", Color.RED, true, 0);
   final Project workProject2 = new Project("Project 2", "", Color.RED, true, 1);
   final Project deletedProject = new Project("Project 3", "", Color.RED, true, 1);

   final HeimatTask heimatTask1 = new HeimatTask(10, "External task name 1", "External project name 1", "PROJECT",
         false, "", false, false);
   final HeimatTask heimatTask2 = new HeimatTask(20, "External task name 2", "External project name 2", "PROJECT",
         false, "", false, false);

   final ExternalProjectMapping project1To1Mapping = new ExternalProjectMapping(ExternalSystem.Heimat,
         heimatTask1.taskHolderName(), heimatTask1.id(), heimatTask1.name(), "", workProject1);
   final ExternalProjectMapping project2To1Mapping = new ExternalProjectMapping(ExternalSystem.Heimat,
         heimatTask1.taskHolderName(), heimatTask1.id(), heimatTask1.name(), "", workProject2);
   final ExternalProjectMapping deletedProjectTo1Mapping = new ExternalProjectMapping(ExternalSystem.Heimat,
         heimatTask1.taskHolderName(), heimatTask1.id(), heimatTask1.name(), "", deletedProject);

   @BeforeEach
   public void beforeEach() {
      externalMappings.clear();
      availableTasks.clear();
      deletedProject.setEnabled(false);

      mockedHeimatSettings = Mockito.mock(HeimatSettings.class);
      mockedHeimatAPI = Mockito.mock(HeimatAPI.class);
      mockedExternalMappingsRepository = Mockito.mock(ExternalProjectsMappingsRepository.class);
      heimatController = new HeimatController(mockedHeimatSettings, mockedHeimatAPI, mockedExternalMappingsRepository,
            new Controller(null, null, null, null), null);

      when(mockedExternalMappingsRepository.findByExternalSystemId(ExternalSystem.Heimat)).thenReturn(externalMappings);

      availableTasks.add(heimatTask1);
      when(mockedHeimatAPI.getMyTasks(now.toLocalDate())).thenReturn(availableTasks);
   }

   /* Map dialog */
   @Test
   void shouldSaveNewMappingWhenMappingDidNotExistBefore() {
      // ARRANGE
      final List<HeimatController.ProjectMapping> newMappings = Arrays.asList(
            new HeimatController.ProjectMapping(workProject1, heimatTask1, false));

      // ACT
      heimatController.updateMappings(newMappings);

      // ASSERT
      ArgumentCaptor<List<ExternalProjectMapping>> saveMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).saveAll(saveMappingsCaptor.capture());
      List<ExternalProjectMapping> savedMappings = saveMappingsCaptor.getValue();
      assertAll(//
            () -> assertThat(savedMappings, Matchers.hasSize(1)) //
            , () -> assertThat(savedMappings.get(0).getProject(), Matchers.is(workProject1)) //
            , () -> assertThat(savedMappings.get(0).getExternalTaskId(), Matchers.is(heimatTask1.id())) //
      );
   }

   @Test
   void shouldNotSaveOrRemoveAnythingWhenMappingDidNotChange() {
      // ARRANGE
      final List<HeimatController.ProjectMapping> newMappings = Arrays.asList(
            new HeimatController.ProjectMapping(workProject1, heimatTask1, false));
      externalMappings.add(project1To1Mapping);

      // ACT
      heimatController.updateMappings(newMappings);

      // ASSERT
      ArgumentCaptor<List<ExternalProjectMapping>> saveMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).saveAll(saveMappingsCaptor.capture());
      List<ExternalProjectMapping> savedMappings = saveMappingsCaptor.getValue();
      ArgumentCaptor<List<ExternalProjectMapping>> deleteMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).deleteAll(deleteMappingsCaptor.capture());
      List<ExternalProjectMapping> deletedMappings = deleteMappingsCaptor.getValue();
      assertAll(//
            () -> assertThat(savedMappings, Matchers.empty()), //
            () -> assertThat(deletedMappings, Matchers.empty()) //
      );
   }

   @Test
   void shouldUpdateMappingWhenExistedBeforeButChanged() {
      // ARRANGE
      final List<HeimatController.ProjectMapping> newMappings = Arrays.asList(
            new HeimatController.ProjectMapping(workProject1, heimatTask2, false));
      externalMappings.add(project1To1Mapping);

      // ACT
      heimatController.updateMappings(newMappings);

      // ASSERT
      ArgumentCaptor<List<ExternalProjectMapping>> saveMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).saveAll(saveMappingsCaptor.capture());
      List<ExternalProjectMapping> savedMappings = saveMappingsCaptor.getValue();
      ArgumentCaptor<List<ExternalProjectMapping>> deleteMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).deleteAll(deleteMappingsCaptor.capture());
      List<ExternalProjectMapping> deletedMappings = deleteMappingsCaptor.getValue();
      assertAll(//
            () -> assertThat(savedMappings, Matchers.hasSize(1)) //
            , () -> assertThat(savedMappings.get(0), Matchers.is(project1To1Mapping)) //
            , () -> assertThat(savedMappings.get(0).getProject(), Matchers.is(workProject1)) //
            , () -> assertThat(savedMappings.get(0).getExternalTaskId(), Matchers.is(heimatTask2.id())) //
            , () -> assertThat(deletedMappings, Matchers.empty()) //
      );
   }

   @Test
   void shouldRemoveMappingWhenMappingDoesNoLongerExist() {
      // ARRANGE
      final List<HeimatController.ProjectMapping> newMappings = Arrays.asList(
            new HeimatController.ProjectMapping(workProject1, null, false));
      externalMappings.add(project1To1Mapping);

      // ACT
      heimatController.updateMappings(newMappings);

      // ASSERT
      ArgumentCaptor<List<ExternalProjectMapping>> saveMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).saveAll(saveMappingsCaptor.capture());
      List<ExternalProjectMapping> savedMappings = saveMappingsCaptor.getValue();
      ArgumentCaptor<List<ExternalProjectMapping>> deleteMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).deleteAll(deleteMappingsCaptor.capture());
      List<ExternalProjectMapping> deletedMappings = deleteMappingsCaptor.getValue();
      assertAll(//
            () -> assertThat(savedMappings, Matchers.empty()) //
            , () -> assertThat(deletedMappings, Matchers.hasSize(1)) //
            , () -> assertThat(deletedMappings.get(0).getProject(), Matchers.is(workProject1)) //
            , () -> assertThat(deletedMappings.get(0).getExternalTaskId(), Matchers.is(heimatTask1.id())) //
      );
   }

   @Test
   void shouldRemoveMappingWhenProjectWasDeleted() {
      // ARRANGE
      final List<HeimatController.ProjectMapping> newMappings = Arrays.asList();
      externalMappings.add(deletedProjectTo1Mapping);

      // ACT
      heimatController.updateMappings(newMappings);

      // ASSERT
      ArgumentCaptor<List<ExternalProjectMapping>> saveMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).saveAll(saveMappingsCaptor.capture());
      List<ExternalProjectMapping> savedMappings = saveMappingsCaptor.getValue();
      ArgumentCaptor<List<ExternalProjectMapping>> deleteMappingsCaptor = ArgumentCaptor.forClass(List.class);
      Mockito.verify(mockedExternalMappingsRepository).deleteAll(deleteMappingsCaptor.capture());
      List<ExternalProjectMapping> deletedMappings = deleteMappingsCaptor.getValue();
      assertAll(//
            () -> assertThat(savedMappings, Matchers.empty()) //
            , () -> assertThat(deletedMappings, Matchers.hasSize(1)) //
            , () -> assertThat(deletedMappings.get(0).getProject(), Matchers.is(deletedProject)) //
            , () -> assertThat(deletedMappings.get(0).getExternalTaskId(), Matchers.is(heimatTask1.id())) //
      );
   }

   /* Sync dialog */
   @Test
   void shouldMarkNonSyncableWhenNotMapped() {
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);

      // ASSERT
      final HeimatController.Mapping mapping = tableRows.get(0);
      assertFalse(mapping.canBeSynced());
      assertFalse(mapping.shouldBeSynced());
      assertThat(mapping.heimatTaskId(), Matchers.is(-1L));
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

      final HeimatController.Mapping mapping = tableRows.get(0);
      assertFalse(mapping.canBeSynced());
      assertFalse(mapping.shouldBeSynced());
      assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString("is not available"));
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
      assertTrue(mapping.shouldBeSynced());
      assertThat(mapping.keeptimeSeconds(), Matchers.is(13 * 60L));
      assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1"));
      assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString(project1To1Mapping.getExternalTaskName()));
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
      assertAll(() -> assertTrue(mapping.canBeSynced()), () -> assertTrue(mapping.shouldBeSynced()),
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
   void shouldDisableShouldBeSyncedWhenAlreadyPresentInHeimat() {
      // ARRANGE
      final Work work1 = new Work(now.minusMinutes(55), now, workProject1, "Notes 1");
      workItems.add(work1);
      externalMappings.add(project1To1Mapping);
      final HeimatTime existingTime1 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 60, "Existing note 1", 12);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime1));

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ASSERT
      assertAll(() -> assertTrue(mapping.canBeSynced()), () -> assertFalse(mapping.shouldBeSynced()),
            () -> assertThat(mapping.keeptimeSeconds(), Matchers.is(55 * 60L)),
            () -> assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1")),
            () -> assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1)),
            () -> assertThat(mapping.heimatNotes(), Matchers.is("Existing note 1")),
            () -> assertThat(mapping.heimatSeconds(), Matchers.is((60) * 60L)),
            () -> assertThat(mapping.existingTimes(), Matchers.containsInAnyOrder(existingTime1))
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
      assertAll(() -> assertTrue(mapping.canBeSynced()), () -> assertTrue(mapping.shouldBeSynced()),
            () -> assertThat(mapping.keeptimeSeconds(), Matchers.is(2 * 13 * 60L)),
            () -> assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1. Notes 2")),
            () -> assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1, workProject2)));
   }

   @Test
   void shouldShowHeimatTimeWhenProjectIsNotMappedInKeeptime() {
      // ARRANGE
      final HeimatTime existingTime1 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 60, "Existing note 1", 12);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime1));
      // there could be more than 1 time for a task in heimat (e.g. when manually saved with start,end feature)
      final HeimatTime existingTime2 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 30, "Existing note 2", 13);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime1, existingTime2));

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ASSERT
      assertAll(() -> assertTrue(mapping.canBeSynced()), () -> assertFalse(mapping.shouldBeSynced()),
            () -> assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString("Not mapped in KeepTime")),
            () -> assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString(project1To1Mapping.getExternalTaskName())),
            () -> assertThat(mapping.keeptimeSeconds(), Matchers.is(0L)),
            () -> assertThat(mapping.keeptimeNotes(), Matchers.is("")),
            () -> assertThat(mapping.projects().size(), Matchers.is(0)),
            () -> assertThat(mapping.heimatNotes(), Matchers.is("Existing note 1. Existing note 2")),
            () -> assertThat(mapping.heimatSeconds(), Matchers.is((60 + 30) * 60L)),
            () -> assertThat(mapping.existingTimes(), Matchers.containsInAnyOrder(existingTime1, existingTime2))
            //
      );
   }

   @Test
   void shouldShowHeimatTimeWhenProjectIsMappedInKeeptimeButNoWorkAtThatDay() {
      // ARRANGE
      final HeimatTime existingTime1 = new HeimatTime(heimatTask1.id(), now.toLocalDate(), null, null, 60,
            "Existing note 1", 12);
      // there could be more than 1 time for a task in heimat (e.g. when manually saved with start,end feature)
      final HeimatTime existingTime2 = new HeimatTime(heimatTask1.id(), now.toLocalDate(), null, null, 30,
            "Existing note 2", 13);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime1, existingTime2));
      externalMappings.add(project1To1Mapping);
      externalMappings.add(project2To1Mapping);

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ASSERT
      assertAll(() -> assertThat(tableRows.size(), Matchers.is(1)), () -> assertTrue(mapping.canBeSynced()),
            () -> assertTrue(mapping.canBeSynced()), () -> assertFalse(mapping.shouldBeSynced()),
            () -> assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString("Present in HEIMAT but not KeepTime")),
            () -> assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString(project1To1Mapping.getExternalTaskName())),
            () -> assertThat(mapping.keeptimeSeconds(), Matchers.is(0L)),
            () -> assertThat(mapping.keeptimeNotes(), Matchers.is("")),
            () -> assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1, workProject2)),
            () -> assertThat(mapping.heimatNotes(), Matchers.is("Existing note 1. Existing note 2")),
            () -> assertThat(mapping.heimatSeconds(), Matchers.is((60 + 30) * 60L)),
            () -> assertThat(mapping.existingTimes(), Matchers.containsInAnyOrder(existingTime1, existingTime2))
            //
      );
   }

   @Test
   void shouldGenerateLinkForDay() {
      when(mockedHeimatSettings.getHeimatUrl()).thenReturn("https://doubleslash.de");
      final String urlForDay = heimatController.getUrlForDay(LocalDate.of(1999, 4, 2));
      assertThat(urlForDay, Matchers.is("https://doubleslash.de/core/heimat/time/main/day/1999/4/2"));
   }

   /* Save */
   @Test
   void shouldSaveTimes() {
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));
      externalMappings.add(project1To1Mapping);
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ACT
      final String userNote = "User entered note";
      final int userMinutes = 15;
      final List<HeimatController.HeimatErrors> errors = heimatController.saveDay(
            List.of(new HeimatController.UserMapping(mapping, true, userNote, userMinutes)), now.toLocalDate());

      // ASSERT
      ArgumentCaptor<HeimatTime> saveMappingsCaptor = ArgumentCaptor.forClass(HeimatTime.class);
      Mockito.verify(mockedHeimatAPI).addMyTime(saveMappingsCaptor.capture());
      assertAll( //
            () -> assertThat(errors, Matchers.empty()) //
            , () -> assertThat(saveMappingsCaptor.getValue().taskId(),
                  Matchers.is(project1To1Mapping.getExternalTaskId())) //
            , () -> assertThat(saveMappingsCaptor.getValue().note(), Matchers.is(userNote)) //
            , () -> assertThat(saveMappingsCaptor.getValue().durationInMinutes(), Matchers.is(userMinutes)) //
      );
   }

   @Test
   void shouldNotSaveTimeWhenUserDoesNotWantToSync() {
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));
      externalMappings.add(project1To1Mapping);
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ACT
      final String userNote = "User entered note";
      final int userMinutes = 15;
      final boolean shouldSync = false;
      heimatController.saveDay(List.of(new HeimatController.UserMapping(mapping, shouldSync, userNote, userMinutes)),
            now.toLocalDate());

      // ASSERT
      Mockito.verify(mockedHeimatAPI, Mockito.never()).addMyTime(any(HeimatTime.class));
   }

   @Test
   void shouldDeleteExistingTimesBeforeSavingWhenTimesAlreadyExist() {
      // updating existing times is not supported. therefore, we delete existing and create new.
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));
      externalMappings.add(project1To1Mapping);

      final HeimatTime existingTime1 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 60, "Existing note 1", 12);
      final HeimatTime existingTime2 = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 30, "Existing note 2", 13);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime1, existingTime2));

      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);

      // ACT
      final String userNote = "User entered note";
      final int userMinutes = 15;
      final List<HeimatController.HeimatErrors> errors = heimatController.saveDay(
            List.of(new HeimatController.UserMapping(mapping, true, userNote, userMinutes)), now.toLocalDate());

      // ASSERT
      InOrder inOrder = inOrder(mockedHeimatAPI);
      inOrder.verify(mockedHeimatAPI).deleteMyTime(existingTime1.id());
      inOrder.verify(mockedHeimatAPI).deleteMyTime(existingTime2.id());

      ArgumentCaptor<HeimatTime> saveMappingsCaptor = ArgumentCaptor.forClass(HeimatTime.class);
      inOrder.verify(mockedHeimatAPI).addMyTime(saveMappingsCaptor.capture());
      assertAll( //
            () -> assertThat(errors, Matchers.empty()) //
            , () -> assertThat(saveMappingsCaptor.getValue().taskId(),
                  Matchers.is(project1To1Mapping.getExternalTaskId())) //
            , () -> assertThat(saveMappingsCaptor.getValue().note(), Matchers.is(userNote)) //
            , () -> assertThat(saveMappingsCaptor.getValue().durationInMinutes(), Matchers.is(userMinutes)) //
      );
   }

   @Test
   void shouldReturnErrorsWhenErrorsOccurred() {
      // ARRANGE
      workItems.add(new Work(now.minusMinutes(13), now, workProject1, "Notes 1"));
      externalMappings.add(project1To1Mapping);
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);
      final HeimatController.Mapping mapping = tableRows.get(0);
      final String exceptionMessage = "SomethingDidNotWork";
      doThrow(new RuntimeException(exceptionMessage)).when(mockedHeimatAPI).addMyTime(any(HeimatTime.class));

      // ACT
      final String userNote = "User entered note";
      final int userMinutes = 15;
      final HeimatController.UserMapping userMapping = new HeimatController.UserMapping(mapping, true, userNote,
            userMinutes);
      final List<HeimatController.HeimatErrors> errors = heimatController.saveDay(List.of(userMapping),
            now.toLocalDate());

      // ASSERT
      ArgumentCaptor<HeimatTime> saveMappingsCaptor = ArgumentCaptor.forClass(HeimatTime.class);
      Mockito.verify(mockedHeimatAPI).addMyTime(saveMappingsCaptor.capture());
      assertAll( //
            () -> assertThat(errors, Matchers.hasSize(1)) //
            , () -> assertThat(errors.get(0).errorMessage(), Matchers.containsString(exceptionMessage)) //
            , () -> assertThat(errors.get(0).mapping(), Matchers.is(userMapping)) //
      );
   }
   // shouldOnlyUpdateHeimatWhenSomethingHasChanged (not needed - user should decide)

   @Test
   void shouldOnlyShowWorkedOnProjectsWhenMultipleProjectsMappedAndSomeHaveWork() {
      // ARRANGE
      // project 1 has work, project 2 does not
      final Work work1 = new Work(now.minusMinutes(10), now, workProject1, "Notes 1");
      workItems.add(work1);

      externalMappings.add(project1To1Mapping);
      externalMappings.add(project2To1Mapping);

      // The mapped Heimat task has already been booked in Heimat
      final HeimatTime existingTime = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 15, "Heimat note", 99);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime));

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);

      // ASSERT
      // There should be exactly one row for this Heimat task
      assertThat(tableRows.size(), Matchers.is(1));
      final HeimatController.Mapping mapping = tableRows.get(0);

      // Only project1 should be in the list since project2 was not worked on
      assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1));
      assertThat(mapping.projects().size(), Matchers.is(1));

      // The mapping should show KeepTime time for workProject1
      assertThat(mapping.keeptimeSeconds(), Matchers.is(10 * 60L));
      assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1"));

      // There should be Heimat time and notes as well
      assertThat(mapping.heimatNotes(), Matchers.is("Heimat note"));
      assertThat(mapping.heimatSeconds(), Matchers.is(15 * 60L));

      assertThat(mapping.syncMessage().toPlainText(), Matchers.not(Matchers.containsString("Present in HEIMAT but not KeepTime")));
      assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString(project1To1Mapping.getExternalTaskName()));
   }

   @Test
   void shouldNotCreateDuplicateHeimatEntryWhenMultipleProjectsMappedAndBothHaveWork() {
      // ARRANGE
      // Both projects have work and are mapped to the same Heimat task
      final Work work1 = new Work(now.minusMinutes(10), now, workProject1, "Notes 1");
      workItems.add(work1);
      final Work work2 = new Work(now.minusMinutes(5), now, workProject2, "Notes 2");
      workItems.add(work2);

      externalMappings.add(project1To1Mapping);
      externalMappings.add(project2To1Mapping);

      // The mapped Heimat task has already been booked in Heimat
      final HeimatTime existingTime = new HeimatTime(project1To1Mapping.getExternalTaskId(), now.toLocalDate(), null,
            null, 15, "Heimat note", 99);
      when(mockedHeimatAPI.getMyTimes(now.toLocalDate())).thenReturn(Arrays.asList(existingTime));

      // ACT
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(now.toLocalDate(), workItems);

      // ASSERT
      // There should be exactly one row for this Heimat task (no duplicates)
      assertThat(tableRows.size(), Matchers.is(1));
      final HeimatController.Mapping mapping = tableRows.get(0);

      // Both projects should be in the list since both were worked on
      assertThat(mapping.projects(), Matchers.containsInAnyOrder(workProject1, workProject2));

      // The mapping should combine KeepTime time from both projects
      assertThat(mapping.keeptimeSeconds(), Matchers.is((10 + 5) * 60L));
      assertThat(mapping.keeptimeNotes(), Matchers.is("Notes 1. Notes 2"));

      // There should be Heimat time and notes as well
      assertThat(mapping.heimatNotes(), Matchers.is("Heimat note"));
      assertThat(mapping.heimatSeconds(), Matchers.is(15 * 60L));

      assertThat(mapping.syncMessage().toPlainText(), Matchers.not(Matchers.containsString("Present in HEIMAT but not KeepTime")));
      assertThat(mapping.syncMessage().toPlainText(), Matchers.containsString(project1To1Mapping.getExternalTaskName()));
   }
}