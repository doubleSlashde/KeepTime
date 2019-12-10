package de.doubleslash.keeptime.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class ManageProjectController {

   private static final Logger LOG = LoggerFactory.getLogger(ManageProjectController.class);

   private Model model;

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

   public String getProjectName() {
      return nameTextField.getText();
   }

   public String getProjectDescription() {
      return descriptionTextArea.getText();
   }

   public Color getProjectColor() {
      return textFillColorPicker.getValue();
   }

   public boolean isWork() {
      return isWorkCheckBox.isSelected();
   }

   public int getIndex() {
      return sortIndexSpinner.getValue();
   }

   public void setModel(final Model model) {
      this.model = model;
   }

   public Project getValues() {
      return new Project(getProjectName(), getProjectDescription(), getProjectColor(), isWork(), getIndex());
   }

   public void setValues(final Project project) {
      LOG.info("Setting values.");
      nameTextField.setText(project.getName());
      descriptionTextArea.setText(project.getDescription());
      textFillColorPicker.setValue(project.getColor());
      isWorkCheckBox.setSelected(project.isWork());
      sortIndexSpinner.getValueFactory().setValue(project.getIndex());
   }

   public void secondInitialize() {
      if (model != null) {
         final int availableProjectAmount = model.getAllProjects().size();
         sortIndexSpinner
               .setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
         sortIndexSpinner.getValueFactory().setValue(model.getAvailableProjects().size());
      }
   }

}
