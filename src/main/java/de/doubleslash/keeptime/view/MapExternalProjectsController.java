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

import de.doubleslash.keeptime.common.ColorHelper;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.controller.HeimatController;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.rest.integration.heimat.model.ExistingAndInvalidMappings;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class MapExternalProjectsController {

   private static final Logger LOG = LoggerFactory.getLogger(MapExternalProjectsController.class);

   private final Model model;
   private final Controller controller;
   private final HeimatController heimatController;

   private Stage thisStage;

   @FXML
   private TableView<HeimatController.ProjectMapping> mappingTableView;

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

   public MapExternalProjectsController(final Model model, Controller controller, HeimatController heimatController) {
      this.model = model;
      this.controller = controller;
      this.heimatController = heimatController;
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

      final List<HeimatTask> externalProjects = heimatController.getTasks(tasksForDateDatePicker.getValue());
      final ExistingAndInvalidMappings existingAndInvalidMappings = heimatController.getExistingProjectMappings(
            externalProjects);
      final List<HeimatController.ProjectMapping> previousProjectMappings = existingAndInvalidMappings.validMappings();

      final ObservableList<HeimatController.ProjectMapping> newProjectMappings = FXCollections.observableArrayList(
            previousProjectMappings);
      final FilteredList<HeimatController.ProjectMapping> value = new FilteredList<>(newProjectMappings,
            pm -> pm.getProject().isWork());
      mappingTableView.setItems(value);

      // KeepTime Project column
      TableColumn<HeimatController.ProjectMapping, String> keepTimeColumn = new TableColumn<>("KeepTime Project");
      keepTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProject().getName()));

      // External Project column with dropdown
      final ObservableList<HeimatTask> externalProjectsObservableList = FXCollections.observableArrayList(
            externalProjects);
      externalProjectsObservableList.add(0, null); // option to clear selection

      TableColumn<HeimatController.ProjectMapping, HeimatTask> externalColumn = new TableColumn<>("HEIMAT Project");
      externalColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getHeimatTask()));
      externalColumn.setCellFactory(col -> new TableCell<>() {
         // TODO search in box would be nice
         private final ComboBox<HeimatTask> comboBox = new ComboBox<>(externalProjectsObservableList);

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
                     setText(item.taskHolderName() + " - " + item.name());
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
                     setText(item.taskHolderName() + " - " + item.name());
                  }
               }
            });

            if (empty) {
               setGraphic(null);
               setText(null);
            } else {
               comboBox.setValue(getTableView().getItems().get(getIndex()).getHeimatTask());
               comboBox.setOnAction(e -> {
                  HeimatController.ProjectMapping mapping = getTableView().getItems().get(getIndex());
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
               setText(item.taskHolderName() + " - " + item.name());
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
               setText(item.taskHolderName() + " - " + item.name());
            }
         }
      });
      addNewProjectButton.disableProperty()
                         .bind(addNewProjectComboBox.getSelectionModel().selectedItemProperty().isNull());
      addNewProjectButton.setOnAction(ae -> {
         final HeimatTask task = addNewProjectComboBox.getValue();
         final int sortIndex = model.getAvailableProjects().size();
         final Project project = controller.addNewProject(
               new Project(task.taskHolderName() + " - " + task.name(), task.bookingHint(), ColorHelper.randomColor(), true,
                     sortIndex));
         newProjectMappings.add(new HeimatController.ProjectMapping(project, task));
         addNewProjectComboBox.getSelectionModel().clearSelection();
      });
      addNewProjectComboBox.setItems(externalProjectsObservableList);

      saveButton.setOnAction(ae -> {
         heimatController.updateMappings(newProjectMappings);
         thisStage.close();
      });

      cancelButton.setOnAction(ae -> thisStage.close());

      List<String> warnings = existingAndInvalidMappings.invalidMappingsAsString();
      if (!warnings.isEmpty()) {
         Platform.runLater(() -> showInvalidMappingsDialog(warnings));
      }
   }

   private void showInvalidMappingsDialog(final List<String> warnings) {
      Dialog<Void> dialog = new Dialog<>();
      dialog.initOwner(this.thisStage);
      dialog.setTitle("Invalid mappings");
      dialog.setHeaderText("Please note to following issue:");

      VBox warningBox = new VBox(10);
      for (String warning : warnings) {
         Label label = new Label(warning);
         label.setWrapText(true);
         warningBox.getChildren().add(label);
      }

      ScrollPane scrollPane = new ScrollPane(warningBox);
      scrollPane.setFitToWidth(true);
      scrollPane.setPrefHeight(Math.min(300, warnings.size() * 30 + 50)); // Adjust height dynamically

      dialog.getDialogPane().setContent(scrollPane);
      dialog.getDialogPane().setMinWidth(400);

      // Add OK button
      ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().add(okButton);

      dialog.showAndWait();
   }
}
