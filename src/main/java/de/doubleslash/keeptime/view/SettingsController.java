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
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Comparator;

import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.ApplicationProperties;
import de.doubleslash.keeptime.common.*;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Settings;
import de.doubleslash.keeptime.view.license.LicenseTableRow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
   private Label hotkeyLabel;
   @FXML
   private Label globalKeyloggerLabel;

   @FXML
   private AnchorPane settingsRoot;

   @FXML
   private Hyperlink gitHubHyperlink;

   @FXML
   private Button reportBugButton;

   @FXML
   private Region bugIcon;

   @FXML
   private Label versionNumberLabel;

   @FXML
   private Hyperlink ourLicenseHyperLink;

   @FXML
   private TableView<LicenseTableRow> licenseTableView;

   @FXML
   private Region colorIcon;

   @FXML
   private Region layoutIcon;

   @FXML
   private Region generalIcon;

   @FXML
   private Region aboutIcon;
   @FXML
   private Region importExportIcon;

   @FXML
   private Region licensesIcon;

   private static final String GITHUB_PAGE = "https://www.github.com/doubleSlashde/KeepTime";
   private static final String GITHUB_ISSUE_PAGE = GITHUB_PAGE + "/issues";
   private static final Color HYPERLINK_COLOR = Color.rgb(0, 115, 170);
   private final ApplicationProperties applicationProperties;

   private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

   private final Controller controller;
   private final Model model;

   private Stage thisStage;

   @Autowired
   ViewController mainscreen;

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
      LOG.debug("set version label text");

      if (!OS.isWindows()) {
         LOG.info("Disabling unsupported settings for Linux.");
         useHotkeyCheckBox.setDisable(true);
         hotkeyLabel.setDisable(true);
         globalKeyloggerLabel.setDisable(true);
         hideProjectsOnMouseExitCheckBox.setDisable(true);
      }

      double requiredWidth = 15.0;
      double requiredHeight = 15.0;

      setRegionSvg(colorIcon, requiredWidth, requiredHeight, RESOURCE.SVG_COLOR_ICON);
      setRegionSvg(layoutIcon, requiredWidth, requiredHeight, RESOURCE.SVG_LAYOUT_ICON);
      setRegionSvg(generalIcon, requiredWidth, requiredHeight, RESOURCE.SVG_SETTINGS_ICON);
      setRegionSvg(aboutIcon, requiredWidth, requiredHeight, RESOURCE.SVG_ABOUT_ICON);
      setRegionSvg(importExportIcon, requiredWidth, requiredHeight, RESOURCE.SVG_IMPORT_EXPORT_ICON);
      setRegionSvg(licensesIcon, requiredWidth, requiredHeight, RESOURCE.SVG_LICENSES_ICON);

      initExportButton();
      initImportButton();

      LOG.debug("saveButton.setOnAction");
      saveButton.setOnAction(ae -> {
         LOG.info("Save clicked");

         if (!OS.isWindows()) {
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
            if (hideProjectsOnMouseExitCheckBox.isSelected()) {
               hideProjectsOnMouseExitCheckBox.setSelected(false);
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
      initializeAbout();
   }

   private static void setRegionSvg(Region region, double requiredWidth, double requiredHeight, RESOURCE resource) {

      region.setShape(SvgNodeProvider.getSvgNodeWithScale(resource, 1.0, 1.0));
      region.setMinSize(requiredWidth, requiredHeight);
      region.setPrefSize(requiredWidth, requiredHeight);
      region.setMaxSize(requiredWidth, requiredHeight);
      region.setStyle("-fx-background-color: black;");
   }

   public void initializeAbout() {
      LOG.debug("set version label");
      versionNumberLabel.setText(applicationProperties.getBuildVersion());

      ourLicenseHyperLink.setFocusTraversable(false);
      ourLicenseHyperLink.setOnAction(ae -> showLicense(Licenses.GPLV3));

      LOG.debug("set up table");
      // name column
      TableColumn<LicenseTableRow, String> nameColumn;
      nameColumn = new TableColumn<>("Name");
      nameColumn.setMinWidth(160);

      nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

      // set SvgPath content
      setRegionSvg(bugIcon, 20.0, 20.0, RESOURCE.SVG_BUG_ICON);

      // licenseColumn
      final TableColumn<LicenseTableRow, String> licenseColumn = new TableColumn<>("License");
      licenseColumn.setMinWidth(260);
      licenseColumn.setCellFactory(param -> new TableCell<LicenseTableRow, String>() {
         @Override
         protected void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);

            setText(empty ? null : item);
            setTextFill(HYPERLINK_COLOR);

            setOnMouseEntered(e -> setUnderline(true));

            setOnMouseExited(e -> setUnderline(false));

            setOnMouseClicked(eventOnMouseClicked -> {
               if (!empty && eventOnMouseClicked.getButton() == MouseButton.PRIMARY) {
                  final LicenseTableRow row = (LicenseTableRow) getTableRow().getItem();
                  final Licenses license = row.getLicense();
                  LOG.debug("License file name: {}", license);

                  showLicense(license);
               }
            });
         }

      });
      licenseColumn.setCellValueFactory(new PropertyValueFactory<>("licenseName"));

      final ObservableList<LicenseTableRow> licenses = loadLicenseRows();

      licenseTableView.setItems(licenses);

      licenseTableView.getColumns().add(nameColumn);
      licenseTableView.getColumns().add(licenseColumn);

      LOG.debug("hyperlink setonaction");
      gitHubHyperlink.setOnAction(ae -> {
         LOG.debug("hyperlink clicked");
         BrowserHelper.openURL(GITHUB_PAGE);
      });

      LOG.debug("roportbugbutton setonaction");
      reportBugButton.setOnAction(ae -> {
         LOG.info("Clicked reportBugButton");
         BrowserHelper.openURL(GITHUB_ISSUE_PAGE);
      });
   }

   private void initImportButton() {
      LOG.debug("Initialize importButton.");
      importButton.setOnAction(event -> {

         try {
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            Stage importConfirmationStage = (Stage) confirmationAlert.getDialogPane().getScene().getWindow();
            importConfirmationStage.getIcons().add(new Image(Resources.getResource(RESOURCE.ICON_MAIN).toString()));

            confirmationAlert.setTitle("Import");
            confirmationAlert.setHeaderText("Do you want to Override current Data ?");
            confirmationAlert.getDialogPane()
                             .setContent(new Label(
                                   """
                                   Import previously exported .sql file. This will overwrite the currently used database contents - all current data will be lost!
                                   
                                   If you do not have a .sql file yet you need to open the previous version of KeepTime and in the settings dialog press "Export".
                                   
                                   You will need to restart the application after this action. If you proceed you need to select the previous exported .sql file.\
                                   """));
            confirmationAlert.showAndWait();

            if (confirmationAlert.getResult() == ButtonType.NO) {
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

            if (file.getName().contains("H2-version-1")) {
               new RunScript().runTool("-url", url, "-user", username, "-password", password, "-script", file.toString(),
                       "-options", "FROM_1X");
               LOG.info("FROM_1X feature is used");
            }else {
               new RunScript().runTool("-url", url, "-user", username, "-password", password, "-script", file.toString());
            }

            Alert informationDialog = new Alert(AlertType.INFORMATION);

            Stage informationStage = (Stage) informationDialog.getDialogPane().getScene().getWindow();
            informationStage.getIcons().add(new Image(Resources.getResource(RESOURCE.ICON_MAIN).toString()));

            informationDialog.setTitle("Import done");
            informationDialog.setHeaderText("The data was imported.");
            informationDialog.getDialogPane()
                             .setContent(new Label("""
                                   KeepTime will now be CLOSED!
                                   You have to RESTART it again to see the changes\
                                   """));
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
            fileChooser.setInitialFileName("KeepTime_database-export_H2-version-%s.sql".formatted(h2Version));
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
            Stage informationStage = (Stage) informationDialog.getDialogPane().getScene().getWindow();
            informationStage.getIcons().add(new Image(Resources.getResource(RESOURCE.ICON_MAIN).toString()));
            informationDialog.setTitle("Export done");
            informationDialog.setHeaderText("The current data was exported.");
            informationDialog.getDialogPane().setContent(new Label("The data was exported to '\n" + fileToSave + "'."));
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
      emptyNoteReminderOnlyForWorkEntryCheckBox.disableProperty()
                                               .bind(emptyNoteReminderCheckBox.selectedProperty().not());
      emptyNoteReminderOnlyForWorkEntryCheckBox.setSelected(model.remindIfNotesAreEmptyOnlyForWorkEntry.get());
      confirmCloseCheckBox.setSelected(model.confirmClose.get());
   }

   public void setStage(final Stage thisStage) {
      this.thisStage = thisStage;
   }

   private FXMLLoader createFXMLLoader(final RESOURCE fxmlLayout) {
      return new FXMLLoader(Resources.getResource(fxmlLayout));
   }

   public ObservableList<LicenseTableRow> loadLicenseRows() {
      final ObservableList<LicenseTableRow> licenseRows = FXCollections.observableArrayList();
      licenseRows.add(new LicenseTableRow("Open Sans", Licenses.APACHEV2));
      licenseRows.add(new LicenseTableRow("jnativehook", Licenses.GPLV3));
      licenseRows.add(new LicenseTableRow("jnativehook", Licenses.LGPLV3));
      licenseRows.add(new LicenseTableRow("commons-lang3", Licenses.APACHEV2));
      licenseRows.add(new LicenseTableRow("flyway-maven-plugin", Licenses.APACHEV2));
      licenseRows.add(new LicenseTableRow("spring-boot-starter-data-jpa", Licenses.APACHEV2));
      licenseRows.add(new LicenseTableRow("mockito-core", Licenses.MIT));
      licenseRows.add(new LicenseTableRow("h2", Licenses.EPLV1));
      licenseRows.add(new LicenseTableRow("Font Awesome Icons", Licenses.CC_4_0));

      licenseRows.sort(Comparator.comparing(LicenseTableRow::getName));

      return licenseRows;
   }

   private void showLicense(final Licenses license) {
      if (!FileOpenHelper.openFile(license.getPath())) {
         final Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("Ooops");
         alert.setHeaderText("Could not find the license file");
         alert.setContentText(
               "We could not find the license file at \"%s\". Did you remove it?%nPlease redownload or visit \"%s\".".formatted(
                     license.getPath(), license.getUrl()));

         alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

         alert.show();
      }
   }
}
