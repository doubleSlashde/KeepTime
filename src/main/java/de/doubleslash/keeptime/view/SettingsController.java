package de.doubleslash.keeptime.view;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.ConfigParser;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
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

   @FXML
   private Button parseConfigButton;

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   private Model model;
   private Controller controller;
   private Stage thisStage;
   private static final String INPUT_FILE = "config.xml";

   @FXML
   private void initialize() {

      saveButton.setOnAction(ae -> {
         Log.info("Save clicked");
         controller.updateSettings(hoverBackgroundColor.getValue(), hoverFontColor.getValue(),
               defaultBackgroundColor.getValue(), defaultFontColor.getValue(), taskBarColor.getValue(),
               useHotkeyCheckBox.isSelected(), displayProjectsRightCheckBox.isSelected());
         thisStage.close();
      });

      cancelButton.setOnAction(ae -> {
         Log.info("Cancel clicked");
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

      parseConfigButton.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(final ActionEvent actionEvent) {
            if (ConfigParser.hasConfigFile(INPUT_FILE)) {
               final ConfigParser parser = new ConfigParser(model, controller);
               parser.parserConfig(new File(INPUT_FILE));
            }
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
