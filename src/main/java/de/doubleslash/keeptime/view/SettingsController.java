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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.common.OS;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Settings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Component
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
   private CheckBox hideProjectsOnMouseExitCheckBox;
   @FXML
   private CheckBox saveWindowPositionCheckBox;

   @FXML
   private CheckBox emptyNoteReminderCheckBox;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   @FXML
   private Button aboutButton;

   @FXML
   private Label hotkeyLabel;
   @FXML
   private Label globalKeyloggerLabel;

   @FXML
   private AnchorPane settingsRoot;

   private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

   private final Controller controller;
   private final Model model;

   private Stage thisStage;

   private Stage aboutStage;

   @Autowired
   public SettingsController(final Model model, final Controller controller) {
      this.model = model;
      this.controller = controller;
   }

   @FXML
   private void initialize() {
      LOG.debug("start init");
      LOG.info("OS: {}", OS.getOSname());
      LOG.debug("set versionLabel text");
      LOG.debug("load substages");
      loadAboutStage();
      LOG.debug("set version label text");

      if (OS.isLinux()) {
         LOG.info("Disabling unsupported settings for Linux.");
         useHotkeyCheckBox.setDisable(true);
         hotkeyLabel.setDisable(true);
         globalKeyloggerLabel.setDisable(true);
      }

      LOG.debug("saveButton.setOnAction");
      saveButton.setOnAction(ae -> {
         LOG.info("Save clicked");

         if (OS.isLinux()) {
            if (hoverBackgroundColor.getValue().getOpacity() < 0.5) {
               hoverBackgroundColor.setValue(Color.rgb((int) (hoverBackgroundColor.getValue().getRed() * 255),
                     (int) (hoverBackgroundColor.getValue().getGreen() * 255),
                     (int) (hoverBackgroundColor.getValue().getBlue() * 255), 0.51));
               final Alert alert = new Alert(AlertType.WARNING);
               alert.setTitle("Warning!");
               alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
               alert.setHeaderText("Color setting not supported!");
               alert.setContentText(
                     "The level of opacity on your hover background is to high for Linux. Resetting it.");

               alert.showAndWait();
            }
            if (defaultBackgroundColor.getValue().getOpacity() < 0.5) {
               defaultBackgroundColor.setValue(Color.rgb((int) (defaultBackgroundColor.getValue().getRed() * 255),
                     (int) (defaultBackgroundColor.getValue().getGreen() * 255),
                     (int) (defaultBackgroundColor.getValue().getBlue() * 255), 0.51));
               final Alert alert = new Alert(AlertType.WARNING);
               alert.setTitle("Warning!");
               alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
               alert.setHeaderText("Color settings not supported!");
               alert.setContentText(
                     "The level of opacity on your hover background is to high for Linux. Resetting it.");

               alert.showAndWait();
            }
            if (!displayProjectsRightCheckBox.isSelected() && hideProjectsOnMouseExitCheckBox.isSelected()) {
               hideProjectsOnMouseExitCheckBox.setSelected(false);
               final Alert warning = new Alert(AlertType.WARNING);
               warning.setTitle("Warning!");
               warning.setHeaderText("No Linux Support");
               warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
               warning.setContentText(
                     "The project list on the left side has no Linux support if projects should be hidden. Disabling hiding of project list.");
               warning.showAndWait();
            }
         }

         controller.updateSettings(new Settings(hoverBackgroundColor.getValue(), hoverFontColor.getValue(),
               defaultBackgroundColor.getValue(), defaultFontColor.getValue(), taskBarColor.getValue(),
               useHotkeyCheckBox.isSelected(), displayProjectsRightCheckBox.isSelected(),
               hideProjectsOnMouseExitCheckBox.isSelected(), model.screenSettings.proportionalX.get(),
               model.screenSettings.proportionalY.get(), model.screenSettings.screenHash.get(),
               saveWindowPositionCheckBox.isSelected(), emptyNoteReminderCheckBox.isSelected()));
         thisStage.close();

      });

      LOG.debug("cancelButton.setOnAction");
      cancelButton.setOnAction(ae -> {
         LOG.info("Cancel clicked");
         thisStage.close();
      });

      LOG.debug("resetButton.setOnAction");
      resetHoverBackgroundButton
            .setOnAction(ae -> hoverBackgroundColor.setValue(Model.ORIGINAL_HOVER_BACKGROUND_COLOR));
      resetHoverFontButton.setOnAction(ae -> hoverFontColor.setValue(Model.ORIGINAL_HOVER_Font_COLOR));
      resetDefaultBackgroundButton
            .setOnAction(ae -> defaultBackgroundColor.setValue(Model.ORIGINAL_DEFAULT_BACKGROUND_COLOR));
      resetDefaultFontButton.setOnAction(ae -> defaultFontColor.setValue(Model.ORIGINAL_DEFAULT_FONT_COLOR));
      resetTaskBarFontButton.setOnAction(ae -> taskBarColor.setValue(Model.ORIGINAL_TASK_BAR_FONT_COLOR));

      LOG.debug("aboutButton.setOnAction");
      aboutButton.setOnAction(ae -> {
         LOG.info("About clicked");
         aboutStage.show();
      });
   }

   void update() {
      // needed to close stage on esc
      settingsRoot.requestFocus();

      hoverBackgroundColor.setValue(model.hoverBackgroundColor.get());
      hoverFontColor.setValue(model.hoverFontColor.get());

      defaultBackgroundColor.setValue(model.defaultBackgroundColor.get());
      defaultFontColor.setValue(model.defaultFontColor.get());

      taskBarColor.setValue(model.taskBarColor.get());

      useHotkeyCheckBox.setSelected(model.useHotkey.get());
      displayProjectsRightCheckBox.setSelected(model.displayProjectsRight.get());
      hideProjectsOnMouseExitCheckBox.setSelected(model.hideProjectsOnMouseExit.get());
      saveWindowPositionCheckBox.setSelected(model.screenSettings.saveWindowPosition.get());
      emptyNoteReminderCheckBox.setSelected(model.remindIfNotesAreEmpty.get());
   }

   public void setStage(final Stage thisStage) {
      this.thisStage = thisStage;
   }

   private void loadAboutStage() {
      try {
         // About stage
         LOG.debug("load about.fxml");
         final FXMLLoader fxmlLoader3 = createFXMLLoader(RESOURCE.FXML_ABOUT);
         fxmlLoader3.setControllerFactory(model.getSpringContext()::getBean);
         LOG.debug("load root");
         final Parent rootAbout = fxmlLoader3.load();
         LOG.debug("set stage");
         aboutStage = new Stage();
         aboutStage.initModality(Modality.APPLICATION_MODAL);
         aboutStage.setTitle("About KeepTime");
         aboutStage.setResizable(false);
         aboutStage.setScene(new Scene(rootAbout));
         aboutStage.setOnHiding(e -> this.thisStage.setAlwaysOnTop(true));
         aboutStage.setOnShowing(e -> {
            this.thisStage.setAlwaysOnTop(false);
            aboutStage.setAlwaysOnTop(false);
         });

         LOG.debug("done setting up stage");
      } catch (final IOException e) {
         throw new FXMLLoaderException("Could not load About stage.", e);
      }

   }

   private FXMLLoader createFXMLLoader(final RESOURCE fxmlLayout) {
      return new FXMLLoader(Resources.getResource(fxmlLayout));
   }
}
