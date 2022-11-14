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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import de.doubleslash.keeptime.Main;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.ApplicationProperties;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
   private CheckBox emptyNoteReminderOnlyForWorkEntryCheckBox;

   @FXML
   private CheckBox confirmCloseCheckBox;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   @FXML
   private Button exportButton;

   @FXML
   private Button importButton;

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
   private final ApplicationProperties applicationProperties;

   private Stage thisStage;

   private Stage aboutStage;

   @Autowired
   ViewController mainscreen;

   @Autowired
   public SettingsController(final Model model, final Controller controller,
         ApplicationProperties applicationProperties) {
      this.model = model;
      this.controller = controller;
      this.applicationProperties = applicationProperties;
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

      initExportButton();
      initImportButton();

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

         if (saveWindowPositionCheckBox.isSelected()) {
            // UPDATE POSITION
            mainscreen.savePosition();
         }

         controller.updateSettings(new Settings(hoverBackgroundColor.getValue(), hoverFontColor.getValue(),
               defaultBackgroundColor.getValue(), defaultFontColor.getValue(), taskBarColor.getValue(),
               useHotkeyCheckBox.isSelected(), displayProjectsRightCheckBox.isSelected(),
               hideProjectsOnMouseExitCheckBox.isSelected(), model.screenSettings.proportionalX.get(),
               model.screenSettings.proportionalY.get(), model.screenSettings.screenHash.get(),
               saveWindowPositionCheckBox.isSelected(), emptyNoteReminderCheckBox.isSelected(),
               emptyNoteReminderOnlyForWorkEntryCheckBox.isSelected(), confirmCloseCheckBox.isSelected()));
         thisStage.close();

      });

      LOG.debug("cancelButton.setOnAction");
      cancelButton.setOnAction(ae ->

      {
         LOG.info("Cancel clicked");
         thisStage.close();
      });

      LOG.debug("resetButton.setOnAction");
      resetHoverBackgroundButton.setOnAction(
            ae -> hoverBackgroundColor.setValue(Model.ORIGINAL_HOVER_BACKGROUND_COLOR));
      resetHoverFontButton.setOnAction(ae -> hoverFontColor.setValue(Model.ORIGINAL_HOVER_Font_COLOR));
      resetDefaultBackgroundButton.setOnAction(
            ae -> defaultBackgroundColor.setValue(Model.ORIGINAL_DEFAULT_BACKGROUND_COLOR));
      resetDefaultFontButton.setOnAction(ae -> defaultFontColor.setValue(Model.ORIGINAL_DEFAULT_FONT_COLOR));
      resetTaskBarFontButton.setOnAction(ae -> taskBarColor.setValue(Model.ORIGINAL_TASK_BAR_FONT_COLOR));

      LOG.debug("aboutButton.setOnAction");
      aboutButton.setOnAction(ae -> {
         LOG.info("About clicked");
         aboutStage.show();
      });
   }
   private void initImportButton(){
      LOG.debug("Initialize importButton.");
      importButton.setOnAction(event ->{

         try {
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION , "", ButtonType.YES, ButtonType.NO);
            confirmationAlert.setTitle("Import");
            confirmationAlert.setHeaderText("Do you want to Override current Data ?");
            confirmationAlert.getDialogPane().setContent(new Label("Import previously exported .sql file. This will overwrite the currently used database contents - all current data will be lost!\n" +
                    "\n" +
                    "If you do not have a .sql file yet you need to open the previous version of KeepTime and in the settings dialog press \"Export\".\n" +
                    "\n" +
                    "You will need to restart the application after this action. If you proceed you need to select the previous exported .sql file."));
            confirmationAlert.initOwner(thisStage);
            confirmationAlert.showAndWait();

            if(confirmationAlert.getResult()==ButtonType.NO){
               LOG.info("User canceled import");
               return;
            }

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Exported SQl Script");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("SQL script files.", "*.sql"));
            File file = fileChooser.showOpenDialog(thisStage);
            if (file == null) {
               LOG.info("User canceled import.");
               return;
            }

            final String url = applicationProperties.getSpringDataSourceUrl();
            final String username = applicationProperties.getSpringDataSourceUserName();
            final String password = applicationProperties.getSpringDataSourcePassword();
            //TODO: add an option at the next release to make the "FROM_1X flag" configurable. E.g. if we upgrade (in the release after) from H2 version 2.x to 2.x we must not set the "FROM_1X flag".
            new RunScript().runTool("-url", url, "-user",username,"-password",password,"-script",file.toString(),"-options", "FROM_1X");

            Alert informationDialog = new Alert(AlertType.INFORMATION);
            informationDialog.setTitle("Import done");
            informationDialog.setHeaderText("The data was imported.");
            informationDialog.getDialogPane().setContent(new Label("KeepTime will now be CLOSED!\n" + "You have to RESTART it again to see the changes"));
            informationDialog.initOwner(thisStage);
            informationDialog.showAndWait();
            Platform.exit();


         } catch (SQLException e) {
            LOG.error("Could not import script file to db.", e);

            Alert errorDialog = new Alert(AlertType.ERROR);
            errorDialog.setTitle("Import failed");
            errorDialog.setHeaderText("The current data could not be imported.");
            errorDialog.setContentText("Please inform a developer and provide your log file.");

            errorDialog.showAndWait();
         }

      });

   }

   private void initExportButton() {
      LOG.debug("Initialize exportButton.");
      exportButton.setOnAction(actionEvent -> {
         LOG.info("Button pressed: exportButton");

         try {
            final String h2Version = applicationProperties.getH2Version();

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(Paths.get(".").toFile());
            fileChooser.setInitialFileName(String.format("KeepTime_database-export_H2-version-%s.sql", h2Version));
            fileChooser.getExtensionFilters().add(new ExtensionFilter("SQL script files.", "*.sql"));
            final File fileToSave = fileChooser.showSaveDialog(thisStage);
            if (fileToSave == null) {
               LOG.info("User canceled export.");
               return;
            }

            final String url = applicationProperties.getSpringDataSourceUrl();
            final String username = applicationProperties.getSpringDataSourceUserName();
            final String password = applicationProperties.getSpringDataSourcePassword();

            LOG.info("Exporting database to '{}'.", fileToSave);
            Script.process(url, username, password, fileToSave.getAbsolutePath(), "DROP", "");
            LOG.info("Export done.");



            Alert informationDialog = new Alert(AlertType.INFORMATION);
            informationDialog.setTitle("Export done");
            informationDialog.setHeaderText("The current data was exported.");
            informationDialog.getDialogPane().setContent(new Label("The data was exported to '\n" + fileToSave + "'."));
            informationDialog.initOwner(thisStage);
            informationDialog.showAndWait();
         } catch (final SQLException e) {
            LOG.error("Could not export db to script file.", e);

            Alert errorDialog = new Alert(AlertType.ERROR);
            errorDialog.setTitle("Export failed");
            errorDialog.setHeaderText("The current data could not be exported.");
            errorDialog.setContentText("Please inform a developer and provide your log file.");

            errorDialog.showAndWait();
         }
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
      emptyNoteReminderOnlyForWorkEntryCheckBox.disableProperty().bind(emptyNoteReminderCheckBox.selectedProperty().not());
      emptyNoteReminderOnlyForWorkEntryCheckBox.setSelected(model.remindIfNotesAreEmptyOnlyForWorkEntry.get());
      confirmCloseCheckBox.setSelected(model.confirmClose.get());
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
