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

package de.doubleslash.keeptime.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import tornadofx.control.DateTimePicker;

public class ManageWorkController {

   private static final Logger LOG = LoggerFactory.getLogger(ManageWorkController.class);

   private Model model;

   @FXML
   private GridPane grid;

   @FXML
   private DateTimePicker startTimePicker;

   @FXML
   private DateTimePicker endTimePicker;

   @FXML
   private DatePicker creationDatePicker;

   @FXML
   private TextField noteTextBox;

   @FXML
   private ComboBox<Project> projectComboBox;

   public void setModel(final Model model) {
      this.model = model;
   }

   public void initializeWith(final Work work) {
      LOG.info("Setting values.");
      startTimePicker.setDateTimeValue(work.getStartTime());
      startTimePicker.setFormat("yyyy-MM-dd HH:mm:ss");
      endTimePicker.setDateTimeValue(work.getEndTime());
      endTimePicker.setFormat("yyyy-MM-dd HH:mm:ss");
      creationDatePicker.setValue(work.getCreationDate());
      noteTextBox.setText(work.getNotes());
      projectComboBox.getItems().addAll(model.getAvailableProjects());

      projectComboBox.setCellFactory(new Callback<ListView<Project>, ListCell<Project>>() {

         @Override
         public ListCell<Project> call(final ListView<Project> l) {
            return new ListCell<Project>() {

               @Override
               protected void updateItem(final Project item, final boolean empty) {
                  super.updateItem(item, empty);
                  if (item == null || empty) {
                     setGraphic(null);
                  } else {
                     setText(item.getName());
                  }
               }
            };
         }
      });

      projectComboBox.setConverter(new StringConverter<Project>() {

         @Override
         public String toString(final Project project) {
            if (project == null) {
               return null;
            } else {
               return project.getName();
            }
         }

         @Override
         public Project fromString(final String string) {
            return null;
         }
      });

      projectComboBox.getSelectionModel().select(work.getProject());

   }

   public Work getWorkFromUserInput() {

      return new Work(creationDatePicker.getValue(), startTimePicker.getDateTimeValue(),
            endTimePicker.getDateTimeValue(), projectComboBox.getSelectionModel().getSelectedItem(),
            noteTextBox.getText());
   }

}
