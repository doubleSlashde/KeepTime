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

import java.util.Comparator;

import de.doubleslash.keeptime.common.*;
import javafx.scene.shape.SVGPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.ApplicationProperties;
import de.doubleslash.keeptime.view.license.LicenseTableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

@Component
public class AboutController {

   private static final String GITHUB_PAGE = "https://www.github.com/doubleSlashde/KeepTime";
   private static final String GITHUB_ISSUE_PAGE = GITHUB_PAGE + "/issues";
   private static final Color HYPERLINK_COLOR = Color.rgb(0, 115, 170);

   @FXML
   private Hyperlink gitHubHyperlink;

   @FXML
   private Button reportBugButton;

   @FXML
   private SVGPath bugIcon;
   @FXML
   private Label versionNumberLabel;

   @FXML
   private Hyperlink ourLicenseHyperLink;

   @FXML
   private TableView<LicenseTableRow> licenseTableView;

   private static final Logger LOG = LoggerFactory.getLogger(AboutController.class);

   private final ApplicationProperties applicationProperties;

   public AboutController(ApplicationProperties applicationProperties) {
      this.applicationProperties = applicationProperties;
   }

   @FXML
   public void initialize() {
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

      //set SvgPath content
      bugIcon.setContent(SvgNodeProvider.getSvgPathWithXMl(Resources.RESOURCE.SVG_BUG_ICON));

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
         alert.setContentText(String.format(
               "We could not find the license file at \"%s\". Did you remove it?%nPlease redownload or visit \"%s\".",
               license.getPath(), license.getUrl()));

         alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

         alert.show();
      }
   }
}
