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

package de.doubleslash.keeptime.view;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.ExternalProjectMapping;
import de.doubleslash.keeptime.model.ExternalSystem;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.model.settings.HeimatSettings;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MapExternalProjectsController {

   private static final Logger LOG = LoggerFactory.getLogger(MapExternalProjectsController.class);

   private final Model model;
   private final Controller controller;
   private final HeimatSettings heimatSettings;
   private final ExternalProjectsMappingsRepository externalProjectsMappingsRepository;

   private Stage thisStage;

   @FXML
   private TableView<ProjectMapping> mappingTableView;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   @FXML
   private ComboBox<HeimatTask> addNewProjectComboBox;

   @FXML
   private Button addNewProjectButton;

   @FXML
   private DatePicker tasksForDateDatePicker;

   public MapExternalProjectsController(final Model model, Controller controller, HeimatSettings heimatSettings,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository) {
      this.model = model;
      this.controller = controller;
      this.heimatSettings = heimatSettings;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
   }

   public void setStage(final Stage thisStage) {
      this.thisStage = thisStage;
   }

   @FXML
   private void initialize() {
      tasksForDateDatePicker.setValue(LocalDate.now());
      tasksForDateDatePicker.setDisable(true);
      // TODO add listener on this thing
      // but what happens with mapped projects not existing at that date? but actually not related to this feature alone

      final HeimatAPI heimatAPI = new HeimatAPI(heimatSettings.getHeimatUrl(), heimatSettings.getHeimatPat());
      final List<HeimatTask> externalProjects = heimatAPI.getMyTasks(tasksForDateDatePicker.getValue());

      final List<ExternalProjectMapping> alreadyMappedProjects = externalProjectsMappingsRepository.findByExternalSystemId(
            ExternalSystem.Heimat);

      final List<ProjectMapping> projectMappings = model.getSortedAvailableProjects().stream().map(p -> {
         final Optional<ExternalProjectMapping> mapping = alreadyMappedProjects.stream()
                                                                               .filter(mp -> mp.getProject().getId()
                                                                                     == p.getId())
                                                                               .findAny();
         if (mapping.isEmpty()) {
            return new ProjectMapping(p, null);
         }
         final Optional<HeimatTask> any = externalProjects.stream()
                                                          .filter(ep -> ep.id() == mapping.get().getExternalTaskId())
                                                          .findAny();
         if (any.isEmpty()) {
            LOG.warn("A mapping exists but task does not exist anymore in HEIMAT! {}.", mapping.get());
            // TODO show this to the user somehow
            return new ProjectMapping(p, null);
         }
         return new ProjectMapping(p, any.get());
      }).toList();

      final ObservableList<ProjectMapping> observableMappings = FXCollections.observableArrayList(projectMappings);
      final FilteredList<ProjectMapping> value = new FilteredList<>(observableMappings, pm->pm.getProject().isWork());
      mappingTableView.setItems(value);

      // KeepTime Project column
      TableColumn<ProjectMapping, String> keepTimeColumn = new TableColumn<>("KeepTime Project");
      keepTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().project.getName()));

      // External Project column with dropdown
      externalProjects.add(0, null); // option to clear selection
      final ObservableList<HeimatTask> externalProjectsObservableList = FXCollections.observableArrayList(externalProjects);
      TableColumn<ProjectMapping, HeimatTask> externalColumn = new TableColumn<>("Heimat Project");
      externalColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().heimatTask));
      externalColumn.setCellFactory(col -> new TableCell<>() {
         // TODO search in box would be nice
         private final ComboBox<HeimatTask> comboBox = new ComboBox<>(
               externalProjectsObservableList );

         @Override
         protected void updateItem(HeimatTask item, boolean empty) {
            super.updateItem(item, empty);
            // selected item
            comboBox.setButtonCell(new ListCell<>() {
               @Override
               protected void updateItem(HeimatTask item, boolean empty) {
                  super.updateItem(item, empty);
                  if (empty || item == null) {
                     setText(null);
                  } else {
                     setText(item.projectName() + " - " + item.name());
                  }
               }
            });

            // Dropdown
            comboBox.setCellFactory(param -> new ListCell<>() {
               @Override
               protected void updateItem(HeimatTask item, boolean empty) {
                  super.updateItem(item, empty);
                  if (item == null || empty) {
                     setGraphic(null);
                     setText(null);
                  } else {
                     // TODO maybe show if the project was already mapped
                     setText(item.projectName() + " - " + item.name());
                  }
               }
            });

            if (empty) {
               setGraphic(null);
               setText(null);
            } else {
               comboBox.setValue(getTableView().getItems().get(getIndex()).getHeimatTask());
               comboBox.setOnAction(e -> {
                  ProjectMapping mapping = getTableView().getItems().get(getIndex());
                  mapping.setHeimatTask(comboBox.getValue());
               });
               setGraphic(comboBox);
               setText(null);
            }
         }
      });

      double scrollbarWidth = 17; // Approximate width of a vertical scrollbar
      keepTimeColumn.prefWidthProperty().bind(mappingTableView.widthProperty().subtract(scrollbarWidth).multiply(.4));
      externalColumn.prefWidthProperty().bind(mappingTableView.widthProperty().subtract(scrollbarWidth).multiply(.6));

      mappingTableView.getColumns().addAll(keepTimeColumn, externalColumn);

      addNewProjectComboBox.setCellFactory(param -> new ListCell<>() {
         @Override
         protected void updateItem(HeimatTask item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
               setGraphic(null);
               setText(null);
            } else {
               // TODO maybe show if the project was already mapped
               setText(item.projectName() + " - " + item.name());
            }
         }
      });
      addNewProjectComboBox.setButtonCell(new ListCell<>() {
         @Override
         protected void updateItem(HeimatTask item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
               setText(null);
            } else {
               setText(item.projectName() + " - " + item.name());
            }
         }
      });
      addNewProjectButton.disableProperty()
                         .bind(addNewProjectComboBox.getSelectionModel().selectedItemProperty().isNull());
      addNewProjectButton.setOnAction(ae -> {
         final HeimatTask task = addNewProjectComboBox.getValue();
         final int sortIndex = model.getAvailableProjects().size();
         final Project project = controller.addNewProject(
               new Project(task.projectName() + " - " + task.name(), task.bookingHint(), Color.BLACK, true, sortIndex));
         observableMappings.add(new ProjectMapping(project, task));
         addNewProjectComboBox.getSelectionModel().clearSelection();
      });
      addNewProjectComboBox.setItems(FXCollections.observableArrayList(externalProjects));

      saveButton.setOnAction((ae) -> {
         LOG.debug("New mappings to be saved '{}'.", observableMappings);

         final List<ExternalProjectMapping> mappingsToCreateOrUpdate = observableMappings.stream()
                                                                                         .filter(
                                                                                               pm -> pm.getHeimatTask()
                                                                                                     != null)
                                                                                         .map(projectMapping -> {
                                                                                            final Optional<ExternalProjectMapping> any = alreadyMappedProjects.stream()
                                                                                                                                                              .filter(
                                                                                                                                                                    pm -> pm.getProject()
                                                                                                                                                                            .getId()
                                                                                                                                                                          == projectMapping.project.getId())
                                                                                                                                                              .findAny();
                                                                                            final HeimatTask heimatTask = projectMapping.getHeimatTask();
                                                                                            if (any.isPresent()) {
                                                                                               final ExternalProjectMapping projectMapping1 = any.get();
                                                                                               projectMapping1.setExternalProjectName(
                                                                                                     heimatTask.projectName());
                                                                                               projectMapping1.setExternalTaskId(
                                                                                                     heimatTask.id());
                                                                                               projectMapping1.setExternalTaskName(
                                                                                                     heimatTask.name());
                                                                                               projectMapping1.setExternalTaskMetadata(
                                                                                                     heimatTask.toString()); // TODO to json
                                                                                               return projectMapping1;
                                                                                            }
                                                                                            return new ExternalProjectMapping(
                                                                                                  ExternalSystem.Heimat,
                                                                                                  heimatTask.projectName(),
                                                                                                  heimatTask.id(),
                                                                                                  heimatTask.name(),
                                                                                                  heimatTask.toString()
                                                                                                  // TODO to json
                                                                                                  ,
                                                                                                  projectMapping.project);
                                                                                         })
                                                                                         .toList();
         // TODO the list also contains unchanged mappings
         externalProjectsMappingsRepository.saveAll(mappingsToCreateOrUpdate);

         // remove mappings which were removed also from database
         final List<ExternalProjectMapping> mappingsToRemove = alreadyMappedProjects.stream()
                                                                                    .filter(
                                                                                          em -> observableMappings.stream()
                                                                                                                  .anyMatch(
                                                                                                                        wantedMapping ->
                                                                                                                              wantedMapping.project.getId()
                                                                                                                                    == em.getProject().getId()
                                                                                                                                    &&
                                                                                                                                    wantedMapping.heimatTask
                                                                                                                                          == null))
                                                                                    .toList();
         externalProjectsMappingsRepository.deleteAll(mappingsToRemove);

         thisStage.close();
      });

      cancelButton.setOnAction(ae -> {
         thisStage.close();
      });
   }

   public static class ProjectMapping {
      Project project;
      HeimatTask heimatTask;

      public ProjectMapping(final Project project, final HeimatTask heimatTask) {
         this.project = project;
         this.heimatTask = heimatTask;
      }

      public Project getProject() {
         return project;
      }

      public void setProject(final Project project) {
         this.project = project;
      }

      public HeimatTask getHeimatTask() {
         return heimatTask;
      }

      public void setHeimatTask(final HeimatTask heimatTask) {
         this.heimatTask = heimatTask;
      }
   }

}
