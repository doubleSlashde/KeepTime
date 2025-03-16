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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
   private final HeimatController heimatController;

   private Stage thisStage;

   @FXML
   private TableView<HeimatController.ProjectMapping> mappingTableView;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

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

      addNewProjectButton.setOnAction(e -> {
         final List<HeimatTask> unmappedHeimatTasks = externalProjects.stream().filter(ht -> {
            final boolean alreadyMapped = value.stream()
                                               .anyMatch(mapping -> mapping.getHeimatTask() != null
                                                     && mapping.getHeimatTask().id() == ht.id());
            return !alreadyMapped;
         }).toList();
         List<HeimatTask> selectedItems = showMultiSelectDialog(externalProjectsObservableList, unmappedHeimatTasks);
         for (HeimatTask toBeCreatedHeimatTask : selectedItems) {
            final int sortIndex = model.getAvailableProjects().size();
            final Project project = controller.addNewProject(
                  new Project(toBeCreatedHeimatTask.name() + " - " + toBeCreatedHeimatTask.taskHolderName(),
                        toBeCreatedHeimatTask.bookingHint(), ColorHelper.randomColor(), true, sortIndex));
            newProjectMappings.add(new HeimatController.ProjectMapping(project, toBeCreatedHeimatTask));
         }
      });

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

   private List<HeimatTask> showMultiSelectDialog(final ObservableList<HeimatTask> externalProjectsObservableList,
         List<HeimatTask> unmappedHeimatTasks) {
      Dialog<List<HeimatTask>> dialog = new Dialog<>();
      dialog.setTitle("Import HEIMAT projects");
      dialog.setHeaderText("You can select mutliple items");
      dialog.initOwner(this.thisStage);
      dialog.setWidth(600);
      dialog.setHeight(500);

      // Buttons
      ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
      ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
      dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

      ListView<HeimatTask> listView = new ListView<>();
      listView.setCellFactory(param -> new ListCell<>() {
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
      listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      listView.setItems(externalProjectsObservableList);

      Button selectAllUnmappedButton = new Button("Select All Unmapped");
      selectAllUnmappedButton.setOnAction(e -> {
         listView.getSelectionModel().clearSelection();
         for (HeimatTask ht : unmappedHeimatTasks) {
            listView.getSelectionModel().select(ht);
         }
         listView.requestFocus();
      });

      VBox content = new VBox(10, selectAllUnmappedButton, listView);
      dialog.getDialogPane().setContent(content);
      final List<HeimatTask> emptyList = List.of();
      // Handle result when OK is clicked
      dialog.setResultConverter(dialogButton -> {
         if (dialogButton == okButtonType) {
            return listView.getSelectionModel().getSelectedItems().stream().toList();
         }
         return emptyList; // Cancel was clicked
      });

      Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
      okButton.setText("Import (0)"); // Initial state

      // Listen for selection changes and update button text
      listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<HeimatTask>) change -> {
         int selectedCount = listView.getSelectionModel().getSelectedItems().size();
         okButton.setText("Import (" + selectedCount + ")");
      });

      Optional<List<HeimatTask>> result = dialog.showAndWait();
      return result.orElse(emptyList);
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
