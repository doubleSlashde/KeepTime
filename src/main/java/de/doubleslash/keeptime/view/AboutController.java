package de.doubleslash.keeptime.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.Main;
import de.doubleslash.keeptime.common.BrowserHelper;
import de.doubleslash.keeptime.common.FileOpenHelper;
import de.doubleslash.keeptime.common.Licenses;
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

   private final FileOpenHelper fileOpen = new FileOpenHelper();
   private final BrowserHelper browserOpen = new BrowserHelper();

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
      TableColumn<LicenceTableRow, String> nameColumn;
      nameColumn = new TableColumn<>("Name");
      nameColumn.setMinWidth(100);
      nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

      // licenseColumn
      TableColumn<LicenceTableRow, Licenses> licenseColumn;
      licenseColumn = new TableColumn<>("License");
      licenseColumn.setMinWidth(200);
      licenseColumn.setCellValueFactory(new PropertyValueFactory<>("license"));

      final ObservableList<LicenceTableRow> licenses = loadRows();

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
               final Licenses license = clickedRow.getLicense();
               LOG.debug("License file name: {}", license.getPath());

               fileOpen.openFile(license.getPath());
            }
         });
         return row;
      });

      LOG.debug("hyperlink setonaction");
      gitHubHyperlink.setOnAction(ae -> {
         LOG.debug("hyperlink clicked");
         browserOpen.openURL(GITHUB_PAGE);
      });

      LOG.debug("roportbugbutton setonaction");
      reportBugButton.setOnAction(ae -> {
         LOG.info("Clicked reportBugButton");
         browserOpen.openURL(GITHUB_PAGE + GITHUB_ISSUE_PAGE);
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
