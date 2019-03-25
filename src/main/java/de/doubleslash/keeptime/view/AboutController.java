package de.doubleslash.keeptime.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.Main;
import de.doubleslash.keeptime.common.BrowserHelper;
import de.doubleslash.keeptime.model.LicenceTableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

public class AboutController {

   private static final String GITHUB_PAGE = "https://www.github.com/doubleSlashde/KeepTime";
   private static final String GITHUB_ISSUE_PAGE = "/issues";

   private static final String APACHE_LICENSE_NAME = "Apache License 2.0";
   private static final String GPL_LICENSE_NAME = "GPL 3.0";

   private static final String LICENSE_PATH = "/licenses/";

   TableColumn<LicenceTableRow, String> nameColumn;
   TableColumn<LicenceTableRow, String> licenseColumn;

   @FXML
   private Hyperlink gitHubHyperlink;

   @FXML
   private Button reportBugButton;

   @FXML
   private Label versionNumberLabel;

   @FXML
   private TableView<LicenceTableRow> licenseTableView;

   public static final Logger LOG = LoggerFactory.getLogger(AboutController.class);

   @FXML
   public void initialize() {
      LOG.debug("set version label");
      versionNumberLabel.setText(Main.VERSION);

      LOG.debug("set up table");
      // name column
      nameColumn = new TableColumn<>("Name");
      nameColumn.setMinWidth(100);
      nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

      // licenseColumn
      licenseColumn = new TableColumn<>("License");
      licenseColumn.setMinWidth(200);
      licenseColumn.setCellValueFactory(new PropertyValueFactory<>("license"));

      final ObservableList<LicenceTableRow> licenses = FXCollections.observableArrayList();
      licenses.add(new LicenceTableRow("jnativehook", "GNU General Public License (GPL), Version 3.0"));
      licenses.add(new LicenceTableRow("jnativehook", "GNU Lesser General Public License (LGPL), Version 3.0"));
      licenses.add(new LicenceTableRow("commons-lang3", "Apache License, Version 2.0"));
      licenses.add(new LicenceTableRow("flyway-maven-plugin", "Apache License, Version 2.0"));
      licenses.add(new LicenceTableRow("spring-boot-starter-data-jpa", "Apache License, Version 2.0"));
      licenses.add(new LicenceTableRow("mockito-core", "The MIT License"));
      licenses.add(new LicenceTableRow("h2", "EPL 1.0"));

      licenseTableView.setItems(licenses);

      licenseTableView.getColumns().add(nameColumn);
      licenseTableView.getColumns().add(licenseColumn);

      LOG.debug("tablerow setonaction");
      licenseTableView.setRowFactory(tv -> {
         LOG.info("table row clicked");
         final TableRow<LicenceTableRow> row = new TableRow<>();
         row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
               final LicenceTableRow clickedRow = row.getItem();
               final String license = clickedRow.getLicense();
               LOG.debug("License file name: " + license + ".txt");
               LOG.debug("full path: " + LICENSE_PATH + license + ".txt");
               LOG.debug(AboutController.class.getResource(LICENSE_PATH + license + ".txt").toString());
               LOG.debug(
                     AboutController.class.getResource(LICENSE_PATH + license + ".txt").toExternalForm().toString());
               BrowserHelper.openURL(AboutController.class.getResource(LICENSE_PATH + license + ".txt").toString());
            }
         });
         return row;
      });

      LOG.debug("hyperlink setonaction");
      gitHubHyperlink.setOnAction(ae -> {
         LOG.debug("hyperlink clicked");
         BrowserHelper.openURL("C:/Users/mplieske/Documents/haha.txt");
      });

      LOG.debug("roportbugbutton setonaction");
      reportBugButton.setOnAction(ae -> {
         LOG.info("Clicked reportBugButton");
         BrowserHelper.openURL(GITHUB_PAGE + GITHUB_ISSUE_PAGE);
      });
   }

   public ObservableList<LicenceTableRow> loadRows() {
      final ObservableList<LicenceTableRow> rows = FXCollections.observableArrayList();

      rows.add(new LicenceTableRow("KeepTime", GPL_LICENSE_NAME));
      rows.add(new LicenceTableRow("jnativehook", GPL_LICENSE_NAME));
      rows.add(new LicenceTableRow("commons-lang3", APACHE_LICENSE_NAME));
      rows.add(new LicenceTableRow("flyway-maven-plugin", APACHE_LICENSE_NAME));
      rows.add(new LicenceTableRow("spring-boot-starter-data-jpa", APACHE_LICENSE_NAME));
      rows.add(new LicenceTableRow("mockito-core", "MIT License"));
      rows.add(new LicenceTableRow("h2", "EPL 1.0"));

      return rows;
   }

}
