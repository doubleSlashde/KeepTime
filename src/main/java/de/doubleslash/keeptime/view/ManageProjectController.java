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

   public void setModel(final Model model) {
      this.model = model;
   }

   public void secondInitialize() {
      if (model != null) {
         final int availableProjectAmount = model.getAllProjects().size();
         sortIndexSpinner
               .setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
         sortIndexSpinner.getValueFactory().setValue(model.getAvailableProjects().size());
      }
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
