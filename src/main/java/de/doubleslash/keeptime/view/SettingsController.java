package de.doubleslash.keeptime.view;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.ConfigParser;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
   private ColorPicker taskBarColor;

   @FXML
   private Button resetHoverBackgroundButton;
   @FXML
   private Button resetHoverFontButton;
   @FXML
   private Button resetDefaultBackgroundButton;
   @FXML
   private Button resetDefaultFontButton;
   @FXML
   private Button resetTaskBarFontButton;

   @FXML
   private CheckBox useHotkeyCheckBox;
   @FXML
   private CheckBox displayProjectsRightCheckBox;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   private final Logger LOG = LoggerFactory.getLogger(this.getClass());

   @FXML
   private Button parseConfigButton;

   private Model model;
   private Controller controller;
   private Stage thisStage;
   private static final String INPUT_FILE = "config.xml";

   @FXML
   private void initialize() {
      saveButton.setOnAction(ae -> {
         LOG.info("Save clicked");

         if (System.getProperty("os.name").contains("Linux")) {
            if (hoverBackgroundColor.getValue().getOpacity() < 0.5) {
               hoverBackgroundColor.setValue(Color.rgb((int) (hoverBackgroundColor.getValue().getRed() * 255),
                     (int) (hoverBackgroundColor.getValue().getGreen() * 255),
                     (int) (hoverBackgroundColor.getValue().getBlue() * 255), 0.51));
               final Alert alert = new Alert(AlertType.WARNING);
               alert.setTitle("Warning!");
               alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
               alert.setHeaderText("color settings wrong!");
               alert.setContentText("The level of opacity on your hover background is to high for Linux.");

               alert.showAndWait();
            }
            if (defaultBackgroundColor.getValue().getOpacity() < 0.5) {
               defaultBackgroundColor.setValue(Color.rgb((int) (defaultBackgroundColor.getValue().getRed() * 255),
                     (int) (defaultBackgroundColor.getValue().getGreen() * 255),
                     (int) (defaultBackgroundColor.getValue().getBlue() * 255), 0.51));
               final Alert alert = new Alert(AlertType.WARNING);
               alert.setTitle("Warning!");
               alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
               alert.setHeaderText("color settings wrong!");
               alert.setContentText("The level of opacity on your default background is to high for Linux.");

               alert.showAndWait();
            }
         }

         if (!displayProjectsRightCheckBox.isSelected()) {
            final Alert warning = new Alert(AlertType.WARNING);
            warning.setTitle("No Linux Support");
            warning.setHeaderText(null);
            warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            warning.setContentText(
                  "The project list on the left side has no Linux support and doesn't run quite well on Windows ether. Please change your settings so, that your project list is on the right side.");
            warning.showAndWait();
         }

         controller.updateSettings(hoverBackgroundColor.getValue(), hoverFontColor.getValue(),
               defaultBackgroundColor.getValue(), defaultFontColor.getValue(), taskBarColor.getValue(),
               useHotkeyCheckBox.isSelected(), displayProjectsRightCheckBox.isSelected());
         thisStage.close();
      });

      cancelButton.setOnAction(ae -> {
         LOG.info("Cancel clicked");
         thisStage.close();
      });

      resetHoverBackgroundButton.setOnAction(ae -> {
         hoverBackgroundColor.setValue(Model.originalHoverBackgroundColor);
      });
      resetHoverFontButton.setOnAction(ae -> {
         hoverFontColor.setValue(Model.originalHoverFontColor);
      });
      resetDefaultBackgroundButton.setOnAction(ae -> {
         defaultBackgroundColor.setValue(Model.originalDefaultBackgroundColor);
      });
      resetDefaultFontButton.setOnAction(ae -> {
         defaultFontColor.setValue(Model.originalDefaultFontColor);
      });
      resetTaskBarFontButton.setOnAction(ae -> {
         taskBarColor.setValue(Model.originalTaskBarFontColor);
      });

      parseConfigButton.setOnAction(ae -> {
         if (ConfigParser.hasConfigFile(INPUT_FILE)) {
            final ConfigParser parser = new ConfigParser(model, controller);
            parser.parserConfig(new File(INPUT_FILE));
         }
      });

   }

   public void setModelAndController(final Model model, final Controller controller) {
      this.model = model;
      this.controller = controller;

      update();
   }

   void update() {
      hoverBackgroundColor.setValue(model.hoverBackgroundColor.get());
      hoverFontColor.setValue(model.hoverFontColor.get());

      defaultBackgroundColor.setValue(model.defaultBackgroundColor.get());
      defaultFontColor.setValue(model.defaultFontColor.get());

      taskBarColor.setValue(model.taskBarColor.get());

      useHotkeyCheckBox.setSelected(model.useHotkey.get());
      displayProjectsRightCheckBox.setSelected(model.displayProjectsRight.get());
   }

   public void setStage(final Stage thisStage) {
      this.thisStage = thisStage;
   }
}
