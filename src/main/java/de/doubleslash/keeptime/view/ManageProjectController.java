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

import de.doubleslash.keeptime.common.RandomColorPicker;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.SvgNodeProvider;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.fxml.FXML;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.GridPane;


import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import static de.doubleslash.keeptime.view.ViewController.fontColorProperty;

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
   private Button randomColorButton;

   @Autowired
   public ManageProjectController(final Model model) {
      this.model = model;
   }

   @FXML
   private void initialize() {
      final int availableProjectAmount = model.getAllProjects().size();
      sortIndexSpinner
            .setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
      sortIndexSpinner.getValueFactory().setValue(model.getAvailableProjects().size());
      randomColorButton.setOnAction(event -> setRandomColor());

      SVGPath calendarSvgPath = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_RANDOM_COLOR_BUTTON, 0.04, 0.04);
      calendarSvgPath.fillProperty().bind(fontColorProperty);
      randomColorButton.setGraphic(calendarSvgPath);
   }

   private void setRandomColor() {
         textFillColorPicker.setValue(RandomColorPicker.chooseContrastColor(model.defaultBackgroundColor.get(), getProjectColorList()));
   }

   private List getProjectColorList(){
      List<Color> colorList = new ArrayList<>();
      for(Project project: model.getAllProjects()){
         colorList.add(project.getColor());
      }
      return colorList;
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

}
