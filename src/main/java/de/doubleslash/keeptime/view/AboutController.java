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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.Main;
import de.doubleslash.keeptime.common.BrowserHelper;
import de.doubleslash.keeptime.common.FileOpenHelper;
import de.doubleslash.keeptime.common.Licenses;
import de.doubleslash.keeptime.view.license.LicenceTableRow;
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
import javafx.scene.paint.Color;

public class AboutController {

   private static final String GITHUB_PAGE = "https://www.github.com/doubleSlashde/KeepTime";
   private static final String GITHUB_ISSUE_PAGE = GITHUB_PAGE + "/issues";
   private static final Color HYPERLINK_COLOR = Color.rgb(0, 115, 170);

   @FXML
   private Hyperlink gitHubHyperlink;

   @FXML
   private Button reportBugButton;

   @FXML
   private Label versionNumberLabel;

   @FXML
   private TableView<LicenceTableRow> licenseTableView;

   private static final Logger LOG = LoggerFactory.getLogger(AboutController.class);

   @FXML
   public void initialize() {
      LOG.debug("set version label");
      versionNumberLabel.setText(Main.VERSION);

      LOG.debug("set up table");
      // name column
      TableColumn<LicenceTableRow, String> nameColumn;
      nameColumn = new TableColumn<>("Name");
      nameColumn.setMinWidth(160);

      nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

      // licenseColumn
      final TableColumn<LicenceTableRow, String> licenseColumn = new TableColumn<>("License");
      licenseColumn.setMinWidth(260);
      licenseColumn.setCellFactory(param -> new TableCell<LicenceTableRow, String>() {
         @Override
         protected void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);

            setText(empty ? null : item);
            setTextFill(HYPERLINK_COLOR);

            setOnMouseEntered(e -> setUnderline(true));

            setOnMouseExited(e -> setUnderline(false));

            setOnMouseClicked(eventOnMouseClicked -> {
               if (!empty && eventOnMouseClicked.getButton() == MouseButton.PRIMARY) {
                  final LicenceTableRow row = (LicenceTableRow) getTableRow().getItem();
                  final Licenses license = row.getLicense();
                  LOG.debug("License file name: {}", license);

                  if (!FileOpenHelper.openFile(license.getPath())) {
                     final Alert alert = new Alert(AlertType.ERROR);
                     alert.setTitle("Ooops");
                     alert.setHeaderText("Could not find the license file");
                     alert.setContentText(
                           String.format("We could not find the license file at \"%s\".%nPlease visit \"%s\".",
                                 license.getPath(), license.getUrl()));

                     alert.show();
                  }
               }
            });
         }
      });
      licenseColumn.setCellValueFactory(new PropertyValueFactory<>("licenseName"));

      final ObservableList<LicenceTableRow> licenses = loadRows();

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

   public ObservableList<LicenceTableRow> loadRows() {
      final ObservableList<LicenceTableRow> rows = FXCollections.observableArrayList();

      rows.add(new LicenceTableRow("KeepTime", Licenses.GPLV3));
      rows.add(new LicenceTableRow("jnativehook", Licenses.GPLV3));
      rows.add(new LicenceTableRow("jnativehook", Licenses.LGPLV3));
      rows.add(new LicenceTableRow("commons-lang3", Licenses.APACHEV2));
      rows.add(new LicenceTableRow("flyway-maven-plugin", Licenses.APACHEV2));
      rows.add(new LicenceTableRow("spring-boot-starter-data-jpa", Licenses.APACHEV2));
      rows.add(new LicenceTableRow("mockito-core", Licenses.MIT));
      rows.add(new LicenceTableRow("h2", Licenses.EPLV1));

      return rows;
   }

}
