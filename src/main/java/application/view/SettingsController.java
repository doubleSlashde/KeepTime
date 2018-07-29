package application.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;

public class SettingsController {
   @FXML
   private ColorPicker color1;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   @FXML
   private void initialize() {

   }
}
