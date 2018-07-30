package application.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;

public class SettingsController {
   @FXML
   private ColorPicker hoverBackgroundColor;
   @FXML
   private ColorPicker hoverFontColor;

   @FXML
   private ColorPicker defaultBackgroundColor;
   @FXML
   private ColorPicker defaultFontColor;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   @FXML
   private void initialize() {
      // TODO can we create an instant preview of the color, while the color dialog is open??
      hoverBackgroundColor.onShownProperty().addListener(a -> {
         Log.info("showing");
      });

      hoverBackgroundColor.onHiddenProperty().addListener(a -> {
         Log.info("hiding");
      });

   }
}
