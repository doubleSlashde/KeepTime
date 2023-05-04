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

package de.doubleslash.keeptime.model;

import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.SettingsRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.paint.Color;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class Model {
   private ProjectRepository projectRepository;
   private WorkRepository workRepository;
   private SettingsRepository settingsRepository;

   public Model(final ProjectRepository projectRepository, final WorkRepository workRepository,
         final SettingsRepository settingsRepository) {
      super();
      this.projectRepository = projectRepository;
      this.workRepository = workRepository;
      this.settingsRepository = settingsRepository;
   }

   public static final Color ORIGINAL_HOVER_BACKGROUND_COLOR = new Color(54 / 255., 143 / 255., 179 / 255., .7);

   public static final Color ORIGINAL_HOVER_Font_COLOR = Color.BLACK;
   public static final Color ORIGINAL_DEFAULT_BACKGROUND_COLOR = new Color(54 / 255., 143 / 255., 179 / 255., .7);
   public static final Color ORIGINAL_DEFAULT_FONT_COLOR = Color.BLACK;

   public static final Color ORIGINAL_TASK_BAR_FONT_COLOR = Color.BLACK;

   public static final Project DEFAULT_PROJECT = new Project("Idle", "", Color.ORANGE, false, 0, true);
   private Project idleProject = DEFAULT_PROJECT;

   private final ObservableList<Project> availableProjects = FXCollections.observableArrayList();
   private final SortedList<Project> sortedAvailableProjects = new SortedList<>(availableProjects,
         Comparator.comparing(Project::getIndex));
   private ObservableList<Project> allProjects = FXCollections.observableArrayList();

   protected final ObservableList<Work> pastWorkItems = FXCollections.observableArrayList();
   private final SortedList<Work> sortedPastWorkItems = new SortedList<>(pastWorkItems,
         Comparator.comparing(Work::getStartTime));
   public final ObjectProperty<Work> activeWorkItem = new SimpleObjectProperty<>();

   public final ObjectProperty<Color> taskBarColor = new SimpleObjectProperty<>(ORIGINAL_TASK_BAR_FONT_COLOR);

   public final ObjectProperty<Color> hoverBackgroundColor = new SimpleObjectProperty<>(
         ORIGINAL_HOVER_BACKGROUND_COLOR);
   public final ObjectProperty<Color> hoverFontColor = new SimpleObjectProperty<>(ORIGINAL_HOVER_Font_COLOR);
   public final ObjectProperty<Color> defaultBackgroundColor = new SimpleObjectProperty<>(
         ORIGINAL_DEFAULT_BACKGROUND_COLOR);
   public final ObjectProperty<Color> defaultFontColor = new SimpleObjectProperty<>(ORIGINAL_DEFAULT_FONT_COLOR);
   public final ObjectProperty<Boolean> useHotkey = new SimpleObjectProperty<>(false);
   public final ObjectProperty<Boolean> displayProjectsRight = new SimpleObjectProperty<>(false);
   public final ObjectProperty<Boolean> hideProjectsOnMouseExit = new SimpleObjectProperty<>(true);

   public final ObjectProperty<Boolean> remindIfNotesAreEmpty = new SimpleObjectProperty<>(false);

   public final ObjectProperty<Boolean> remindIfNotesAreEmptyOnlyForWorkEntry = new SimpleObjectProperty<>(false);

   public final ObjectProperty<Boolean> confirmClose = new SimpleObjectProperty<>(false);

   public final ScreenSettings screenSettings = new ScreenSettings();

   private ConfigurableApplicationContext springContext;

   public void setWorkRepository(final WorkRepository workRepository) {
      this.workRepository = workRepository;
   }

   public void setProjectRepository(final ProjectRepository projectRepository) {
      this.projectRepository = projectRepository;
   }

   public void setSettingsRepository(final SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;
   }

   public void setIdleProject(final Project idleProject) {
      this.idleProject = idleProject;
   }

   public void setAllProjects(final ObservableList<Project> allProjects) {
      this.allProjects = allProjects;
   }

   public WorkRepository getWorkRepository() {
      return workRepository;
   }

   public SortedList<Project> getSortedAvailableProjects() {
      return sortedAvailableProjects;
   }

   public ProjectRepository getProjectRepository() {
      return projectRepository;
   }

   public SettingsRepository getSettingsRepository() {
      return settingsRepository;
   }

   public ObservableList<Work> getPastWorkItems() {
      return pastWorkItems;
   }

   public Project getIdleProject() {
      return idleProject;
   }

   public ObservableList<Project> getAvailableProjects() {
      return availableProjects;
   }

   public ObservableList<Project> getAllProjects() {
      return allProjects;
   }

   public void setSpringContext(final ConfigurableApplicationContext springContext) {
      this.springContext = springContext;
   }

   public ConfigurableApplicationContext getSpringContext() {
      return this.springContext;
   }

   public SortedList<Work> getSortedPastWorkItems() {
      return sortedPastWorkItems;
   }

}
