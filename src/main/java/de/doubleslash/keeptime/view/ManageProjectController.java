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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.fxml.FXML;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.GridPane;

@Component
public class ManageProjectController {

   private static final Logger LOG = LoggerFactory.getLogger(ManageProjectController.class);

   private final Model model;

   @FXML
   private GridPane grid;

   @FXML
   private TextField nameTextField;

   @FXML
   private TextArea descriptionTextArea;

   @FXML
   private ColorPicker textFillColorPicker;

   @FXML
   private CheckBox isWorkCheckBox;

   @FXML
   private Spinner<Integer> sortIndexSpinner;

   @FXML
   private Label validateTextAlert;

   private BooleanProperty formValidProperty = new SimpleBooleanProperty(false);

   public ManageProjectController(final Model model) {
      this.model = model;
   }
   @FXML
   private void initialize() {
      final int availableProjectAmount = model.getAllProjects().size();
      sortIndexSpinner
              .setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
      sortIndexSpinner.getValueFactory().setValue(model.getAvailableProjects().size());
      formValidProperty.bind(Bindings.createBooleanBinding(() -> !nameTextField.getText().isBlank(),nameTextField.textProperty()));
      validateTextAlert.visibleProperty().bind(formValidProperty.not());
      
      Platform.runLater(() ->{
            nameTextField.requestFocus();
            nameTextField.end();
      });
   }

   public void initializeWith(final Project project) {
      LOG.info("Setting values.");
      nameTextField.setText(project.getName());
      descriptionTextArea.setText(project.getDescription());
      textFillColorPicker.setValue(project.getColor());
      isWorkCheckBox.setSelected(project.isWork());
      sortIndexSpinner.getValueFactory().setValue(project.getIndex());
   }

   public Project getProjectFromUserInput() {
      return new Project(nameTextField.getText(), descriptionTextArea.getText(), textFillColorPicker.getValue(),
            isWorkCheckBox.isSelected(), sortIndexSpinner.getValue());
   }

   public BooleanProperty formValidProperty() {
      return formValidProperty;
   }

}
