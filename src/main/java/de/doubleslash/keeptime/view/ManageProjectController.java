package de.doubleslash.keeptime.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.model.Model;
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

   private static final int DEFAULT_SPINNER_VALUE = 0;

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

   public GridPane getGrid() {
      return grid;
   }

   public TextField getNameTextField() {
      return nameTextField;
   }

   public TextArea getDescriptionTextArea() {
      return descriptionTextArea;
   }

   public ColorPicker getTextFillColorPicker() {
      return textFillColorPicker;
   }

   public CheckBox getIsWorkCheckBox() {
      return isWorkCheckBox;
   }

   public Spinner<Integer> getSortIndexSpinner() {
      return sortIndexSpinner;
   }

   public void setModel(final Model model) {
      this.model = model;
   }

   public void setValues(final String name, final String description, final Color textFill, final boolean work,
         final int sortIndex) {
      LOG.info("Setting values.");
      nameTextField.setText(name);
      descriptionTextArea.setText(description);
      textFillColorPicker.setValue(textFill);
      isWorkCheckBox.setSelected(work);
      sortIndexSpinner.getValueFactory().setValue(sortIndex);
   }

   public void secondInitialize() {
      if (model != null) {
         final int availableProjectAmount = model.getAllProjects().size();
         sortIndexSpinner
               .setValueFactory(new IntegerSpinnerValueFactory(DEFAULT_SPINNER_VALUE, availableProjectAmount, availableProjectAmount));
         sortIndexSpinner.getValueFactory().setValue(model.getAvailableProjects().size());
      }
   }

}
