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

package de.doubleslash.keeptime.viewpopup;

import java.awt.Point;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ViewControllerPopup {

   private static final int LIST_CELL_HEIGHT = 23 + 2;

   private static final Logger LOG = LoggerFactory.getLogger(ViewControllerPopup.class);

   @FXML
   private Pane pane;

   @FXML
   private TextField searchTextField;
   @FXML
   private ListView<Project> projectListView;

   private Stage stage;

   private Controller controller;

   private FilteredList<Project> filteredData;

   private void changeProject(final Project item) {
      LOG.info("Change project to '{}'.", item.getName());

      // ask for a note for the current project
      final TextInputDialog dialog = new TextInputDialog(Model.activeWorkItem.get().getNotes());
      dialog.setTitle("Note for current project");
      dialog.setHeaderText(
            "Add a note for '" + Model.activeWorkItem.get().getProject().getName() + "' before changing project?");
      dialog.setContentText("Note: ");

      this.stage.setAlwaysOnTop(false);
      final Optional<String> result = dialog.showAndWait();
      this.stage.setAlwaysOnTop(true);

      result.ifPresent(note -> {
         controller.setComment(note);
         hide();
         controller.changeProject(item);
      });
   }

   @FXML
   private void initialize() {

      // TODO the cells do not refresh, if a project was changed
      projectListView.setCellFactory(cb -> returnListCellOfProject());

      // TODO why is there no nice way for listview height?
      // https://stackoverflow.com/questions/17429508/how-do-you-get-javafx-listview-to-be-the-height-of-its-items
      final Consumer<Double> updateSize = height -> {
         if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s%f", "update size ", height));
            projectListView.setPrefHeight(height);
            stage.sizeToScene(); // also update scene size
         }
      };

      searchTextField.textProperty().addListener((a, b, newValue) -> {
         // TODO do i always have to create a new predicate?
         filteredData.setPredicate(project -> {
            // If filter text is empty, display all persons.
            boolean returnValue = false;
            if (newValue == null || newValue.isEmpty()) {
               returnValue = true;
            }

            final String lowerCaseFilter = newValue.toLowerCase();

            if (project.getName().toLowerCase().contains(lowerCaseFilter)) {
               returnValue = true; // Filter matches first name.
            }

            return returnValue; // Does not match.
         });

         projectListView.getSelectionModel().selectFirst();
         updateSize.accept((double) (filteredData.size() * LIST_CELL_HEIGHT));
      });

      searchTextField.setOnKeyPressed(eh -> {
         final MultipleSelectionModel<Project> selectionModel = projectListView.getSelectionModel();
         final int selectedIndex = selectionModel.getSelectedIndex();
         switch (eh.getCode()) {
         case UP:
            selectionModel.select(selectedIndex - 1);
            eh.consume();
            break;
         case DOWN:
            selectionModel.select(selectedIndex + 1);
            eh.consume();
            break;
         case ESCAPE:
            hide();
            break;
         default:
            break;

         }
      });
      // enter pressed in textfield
      searchTextField.setOnAction(ev -> {
         final Project selectedItem = projectListView.getSelectionModel().getSelectedItem();
         if (selectedItem != null) {
            changeProject(selectedItem);
         }
      });

      projectListView.getSelectionModel().selectFirst();
      projectListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

   }

   public ListCell<Project> returnListCellOfProject() {

      return new ListCell<Project>() {

         @Override
         protected void updateItem(final Project project, final boolean empty) {
            super.updateItem(project, empty);

            if (project == null || empty) {
               setText(null);

            } else {
               setOnMouseClicked(ev -> changeProject(project));

               final boolean isActiveProject = project == Model.activeWorkItem.get().getProject();
               setText((isActiveProject ? "* " : "") + project.getName());
               setTextFill(project.getColor());
               setUnderline(project.isWork());
            }
         }

      };

   }

   public void setControllerAndModel(final Controller controller, final Model model) {
      this.controller = controller;

      filteredData = new FilteredList<>(model.getSortedAvailableProjects(), p -> true);
      projectListView.setItems(filteredData);
   }

   public void setStage(final Stage primaryStage) {
      this.stage = primaryStage;

      // if we loose focus, hide the stage
      stage.focusedProperty().addListener((a, b, newValue) -> {
         if (!newValue) {
            hide();
         }
      });
   }

   public void show(final Point mouseLocation) {
      if (!stage.isShowing()) {
         LOG.info("Showing popup");
         projectListView.getSelectionModel().select(0);
         projectListView.refresh();

         searchTextField.setText("a"); // trigger to update list size
         searchTextField.setText("");

         stage.setX(mouseLocation.getX() - 2);
         stage.setY(mouseLocation.getY() - 2);
         stage.show();
         stage.requestFocus();
         searchTextField.requestFocus();
      }
   }

   private void hide() {
      if (stage.isShowing()) {

         searchTextField.setText("");

         stage.hide();
      }
   }

}
