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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.doubleslash.keeptime.model.settings.HeimatSettings;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.DateProvider;
import de.doubleslash.keeptime.common.time.Interval;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Settings;
import de.doubleslash.keeptime.model.Work;
import jakarta.annotation.PreDestroy;

@Service
public class Controller {

   private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

   private final long AUTO_SAVE_INTERVAL_SECONDS = 60;
   private final HeimatSettings heimatSettings;
   private Interval autoSaveInterval;

   private final Model model;
   private final Settings settings;

   private final DateProvider dateProvider;

   public Controller(final Model model, Settings settings, HeimatSettings heimatSettings, final DateProvider dateProvider) {
      this.model = model;
      this.settings = settings;
      this.heimatSettings = heimatSettings;
      this.dateProvider = dateProvider;
   }

   public void enableAutoSave() {
      LOG.info("Enabling auto save with interval '{}' seconds.", AUTO_SAVE_INTERVAL_SECONDS);
      autoSaveInterval = new Interval(AUTO_SAVE_INTERVAL_SECONDS);
      autoSaveInterval.registerCallBack(() -> {
         LOG.debug("Auto saving current work.");
         saveCurrentWork(dateProvider.dateTimeNow());
      });
   }

   public void changeProject(final Project newProject) {
      changeProject(newProject, 0);
   }

   public void changeProject(final Project newProject, final long minusSeconds) {

      final LocalDateTime workEnd = dateProvider.dateTimeNow().minusSeconds(minusSeconds);
      final LocalDate today = dateProvider.dateTimeNow().toLocalDate();

      final Work oldWork = saveCurrentWork(workEnd);

      if (oldWork != null && !today.isEqual(oldWork.getStartTime().toLocalDate())) {
         LOG.info("Removing projects with other creation date than today '{}' from list.", today);
         final int sizeBefore = model.getPastWorkItems().size();
         model.getPastWorkItems().removeIf(w -> !today.isEqual(w.getStartTime().toLocalDate()));
         LOG.debug("Removed '{}' work items from past work items.", sizeBefore - model.getPastWorkItems().size());
      }

      // Start new work
      final Work newWork = new Work(workEnd, workEnd.plusSeconds(minusSeconds), newProject, "");

      model.getPastWorkItems().add(newWork);
      model.activeWorkItem.set(newWork);
   }

   public Work saveCurrentWork(final LocalDateTime workEnd) {
      final Work currentWork = model.activeWorkItem.get();

      if (currentWork == null) {
         return null;
      }

      currentWork.setEndTime(workEnd);

      final String time = DateFormatter.secondsToHHMMSS(
            Duration.between(currentWork.getStartTime(), currentWork.getEndTime()).getSeconds());

      LOG.info("Saving Work from '{}' to '{}' ({}) on project '{}' with notes '{}'", currentWork.getStartTime(),
            currentWork.getEndTime(), time, currentWork.getProject().getName(), currentWork.getNotes());

      // Save in db
      return model.getWorkRepository().save(currentWork);
   }

   public Project addNewProject(final Project project) {
      LOG.info("Creating new project '{}'.", project);

      model.getAllProjects().add(project);
      model.getAvailableProjects().add(project);

      final List<Project> changedProjects = resortProjectIndexes(model.getAvailableProjects(), project,
            model.getAvailableProjects().size(), project.getIndex());
      changedProjects.add(project);
      model.getProjectRepository().saveAll(changedProjects);
      return project;
   }


   public void updateColorSettings(final Color hoverBackgroundColor,final Color hoverFontColor,final Color defaultBackgroundColor,final Color defaultFontColor,final Color taskBarColor) {
      settings.setTaskBarColor(taskBarColor);
      settings.setDefaultBackgroundColor(defaultBackgroundColor);
      settings.setDefaultFontColor(defaultFontColor);
      settings.setHoverBackgroundColor(hoverBackgroundColor);
      settings.setHoverFontColor(hoverFontColor);
      settings.save();

      model.defaultBackgroundColor.set(settings.getDefaultBackgroundColor());
      model.defaultFontColor.set(settings.getDefaultFontColor());
      model.hoverBackgroundColor.set(settings.getHoverBackgroundColor());
      model.hoverFontColor.set(settings.getHoverFontColor());
      model.taskBarColor.set(settings.getTaskBarColor());
   }

   public void updateLayoutSettings(final boolean displayProjectsRight,final boolean  hideProjectsOnMouseExit,final double proportionalX,final double proportionalY,final int screenHash,final boolean  saveWindowPosition) {
      settings.setDisplayProjectsRight(displayProjectsRight);
      settings.setHideProjectsOnMouseExit(hideProjectsOnMouseExit);
      settings.setSaveWindowPosition(saveWindowPosition);
      settings.setWindowXProportion(proportionalX);
      settings.setWindowYProportion(proportionalY);
      settings.setScreenHash(screenHash);
      settings.save();

      model.displayProjectsRight.set(settings.isDisplayProjectsRight());
      model.hideProjectsOnMouseExit.set(settings.isHideProjectsOnMouseExit());
      model.screenSettings.saveWindowPosition.set(settings.isSaveWindowPosition());
      model.screenSettings.proportionalX.set(settings.getWindowXProportion());
      model.screenSettings.proportionalY.set(settings.getWindowYProportion());
      model.screenSettings.screenHash.set(settings.getScreenHash());
   }

   public void updateFeatureSettings(final boolean useHotkey,final boolean emptyNoteReminder,final boolean emptyNoteReminderOnlyForWorkEntry,final boolean confirmClose) {
      settings.setUseHotkey(useHotkey);
      settings.setRemindIfNotesAreEmpty(emptyNoteReminder);
      settings.setRemindIfNotesAreEmptyOnlyForWorkEntry(emptyNoteReminderOnlyForWorkEntry);
      settings.setConfirmClose(confirmClose);
      settings.save();

      model.useHotkey.set(settings.isUseHotkey());
      model.remindIfNotesAreEmpty.set(settings.isRemindIfNotesAreEmpty());
      model.remindIfNotesAreEmptyOnlyForWorkEntry.set(settings.isRemindIfNotesAreEmptyOnlyForWorkEntry());
      model.confirmClose.set(settings.isConfirmClose());
   }


   public void updateHeimatSettings(final boolean active, final String url, final String pat){
      heimatSettings.setHeimatActive(active);
      heimatSettings.setHeimatUrl(url);
      heimatSettings.setHeimatPat(pat);
      heimatSettings.save();
   }

   @PreDestroy
   public void shutdown() {
      LOG.info("Controller shutdown");

      LOG.info("Changing project to persist current work on shutdown.");
      changeProject(model.getIdleProject(), 0);

      LOG.info("Updating settings to persist local changes on shutdown.");
      // these are changed while dragging the windows - not via Settings-Dialog. Therefore, we need to save them separately.
      settings.setScreenHash(model.screenSettings.screenHash.get());
      settings.setWindowXProportion(model.screenSettings.proportionalX.get());
      settings.setWindowYProportion(model.screenSettings.proportionalY.get());
      settings.save();
   }

   public void deleteProject(final Project p) {
      if (p.isDefault()) {
         LOG.error("You cannot delete the default project. Tried to delete project '{}'", p);
         return;
      }

      if (isProjectActive(p)) {
         changeProject(model.getIdleProject());
      }

      LOG.info("Disabling project '{}'.", p);

      final int indexToRemove = p.getIndex();
      p.setEnabled(false); // we don't delete it because of the referenced work
      // items
      p.setIndex(-1);

      model.getAvailableProjects().remove(p);

      final List<Project> changedProjects = adaptProjectIndexesAfterRemoving(model.getAvailableProjects(),
            indexToRemove);

      changedProjects.add(p);
      model.getProjectRepository().saveAll(changedProjects);
   }

   private boolean isProjectActive(final Project p) {
      return p == model.activeWorkItem.get().getProject();
   }

   public void editProject(final Project projectToBeUpdated, final Project newValuedProject) {
      LOG.info("Changing project '{}' to '{}'.", projectToBeUpdated, newValuedProject);

      projectToBeUpdated.setName(newValuedProject.getName());
      projectToBeUpdated.setDescription(newValuedProject.getDescription());
      projectToBeUpdated.setColor(newValuedProject.getColor());
      projectToBeUpdated.setWork(newValuedProject.isWork());
      final int oldIndex = projectToBeUpdated.getIndex();
      projectToBeUpdated.setIndex(newValuedProject.getIndex());

      final List<Project> changedProjects = resortProjectIndexes(model.getAvailableProjects(), projectToBeUpdated,
            oldIndex, newValuedProject.getIndex());
      changedProjects.add(projectToBeUpdated);

      // save all projects which changed index
      model.getProjectRepository().saveAll(changedProjects);
   }

   public void editWork(final Work workToBeEdited, final Work newValuedWork) {
      LOG.info("Changing work '{}' to '{}'.", workToBeEdited, newValuedWork);

      workToBeEdited.setStartTime(newValuedWork.getStartTime());
      workToBeEdited.setEndTime(newValuedWork.getEndTime());
      workToBeEdited.setNotes(newValuedWork.getNotes());
      workToBeEdited.setProject(newValuedWork.getProject());

      final Work editedWork = model.getWorkRepository().save(workToBeEdited);

      // remove old
      model.getPastWorkItems().removeIf(w -> (w.getId() == workToBeEdited.getId()));
      // add if started today
      final LocalDate dateNow = dateProvider.dateTimeNow().toLocalDate();
      if (dateNow.equals(editedWork.getStartTime().toLocalDate())) {
         model.getPastWorkItems().add(editedWork);
      }

   }

   public void deleteWork(final Work workToBeDeleted) {
      LOG.info("Deleting work '{}'.", workToBeDeleted);

      model.getPastWorkItems().removeIf(w -> (w.getId() == workToBeDeleted.getId()));
      model.getWorkRepository().delete(workToBeDeleted);
   }

   /**
    * Changes the indexes of the originalList parameter to have a consistent order.
    *
    * @param originalList
    *           list of all projects to adapt the indexes for
    * @param changedProject
    *           the project which has changed which already has the new index
    * @param oldIndex
    *           the old index of the changed project
    * @param newIndex
    *           the new index of the changed project (which the projects also already has)
    * @return all projects whose index has been adapted
    */
   List<Project> resortProjectIndexes(final List<Project> originalList, final Project changedProject,
         final int oldIndex, final int newIndex) {
      final ArrayList<Project> changedProjects = new ArrayList<>(Math.abs(oldIndex - newIndex));
      if (newIndex == oldIndex) {
         return changedProjects;
      }

      final boolean newIndexGreater = newIndex > oldIndex;
      final int adjustOffset = newIndexGreater ? -1 : +1;

      final int smallerIndex = Math.min(newIndex, oldIndex);
      final int biggerIndex = Math.max(newIndex, oldIndex);

      for (int i = 0; i < originalList.size(); i++) {
         final Project project = originalList.get(i);
         final int currentIndex = project.getIndex();
         if ((currentIndex < smallerIndex || currentIndex > biggerIndex) || project == changedProject) {
            // index is not affected by change or
            // this one is already at the right/wanted index
            continue;
         }

         final int newCurrentIndex = currentIndex + adjustOffset;
         project.setIndex(newCurrentIndex);
         changedProjects.add(project);
      }

      return changedProjects;
   }

   /**
    * Decreases all indexes by one, after the removed index
    *
    * @param originalList
    *           list of all projects to adapt the indexes for
    * @param removedIndex
    *           the index which has been removed
    * @return all projects whose index has been adapted
    */
   List<Project> adaptProjectIndexesAfterRemoving(final List<Project> originalList, final int removedIndex) {
      final int size = originalList.size();
      final List<Project> changedProjects = new ArrayList<>(size - removedIndex);
      for (int i = 0; i < size; i++) {
         final Project project = originalList.get(i);
         if (project.getIndex() < removedIndex) {
            // Not affected
            continue;
         }

         project.setIndex(project.getIndex() - 1);
         changedProjects.add(project);
      }
      return changedProjects;
   }

   public void setComment(final String notes) {
      final Work work = model.activeWorkItem.get();
      work.setNotes(notes);
   }

   /**
    * Calculate today's seconds counted as work
    */
   public long calcTodaysWorkSeconds() {
      final List<Work> workItems = new ArrayList<>();

      for (final Work work : model.getPastWorkItems()) {
         final Project project = work.getProject();
         for (final Project p : model.getAllProjects()) {
            if (p.getId() == project.getId()) {
               if (p.isWork()) {
                  workItems.add(work);
               }
               break;
            }
         }
      }

      return calcSeconds(workItems);
   }

   /**
    * Calculate today's present seconds (work+nonWork)
    */
   public long calcTodaysSeconds() {
      return calcSeconds(model.getPastWorkItems());
   }

   public long calcSeconds(final List<Work> workItems) {
      long seconds = 0;

      for (final Work w : workItems) {
         seconds += Duration.between(w.getStartTime(), w.getEndTime()).getSeconds();
      }

      return seconds;
   }


}
