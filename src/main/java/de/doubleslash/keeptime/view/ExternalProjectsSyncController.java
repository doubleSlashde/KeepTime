package de.doubleslash.keeptime.view;

import de.doubleslash.keeptime.model.*;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.model.settings.HeimatSettings;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.List;

@Component
public class ExternalProjectsSyncController {

   private static final Logger LOG = LoggerFactory.getLogger(MapExternalProjectsController.class);

   @FXML
   private TableView<TableRow> mappingTableView;

   @FXML
   private Button saveButton;

   @FXML
   private Button cancelButton;

   @FXML
   private Label dayOfSyncLabel;

   @FXML
   private Label sumTimeLabel;

   @FXML
   private Hyperlink externalSystemLink;

   private final Model model;
   private final HeimatSettings heimatSettings;
   private final ExternalProjectsMappingsRepository externalProjectsMappingsRepository;

   private final LocalTimeStringConverter localTimeStringConverter = new LocalTimeStringConverter(FormatStyle.SHORT);

   public ExternalProjectsSyncController(final Model model, HeimatSettings heimatSettings,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository) {
      this.model = model;
      this.heimatSettings = heimatSettings;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
   }

   @FXML
   private void initialize() {
      // TODO set this from ReportController
      final LocalDate currentReportDate = LocalDate.now();
      final List<Work> currentWorkItems = model.getWorkRepository()
                                               .findByStartDateOrderByStartTimeAsc(currentReportDate);

      final HeimatAPI heimatAPI = new HeimatAPI(heimatSettings.getHeimatUrl(), heimatSettings.getHeimatPat());
      //final List<HeimatTask> externalProjects = heimatAPI.getMyTasks(currentReportDate);
      final List<ExternalProjectMapping> mappedProjects = externalProjectsMappingsRepository.findByExternalSystemId(
            ExternalSystem.Heimat);
      // TODO check if external projects are available for the currentDay

      final TableRow tableRow = new TableRow();
      tableRow.project = model.activeWorkItem.get().getProject();
      tableRow.shouldSyncCheckBox = new SimpleBooleanProperty(true);
      tableRow.syncStatus = new SimpleStringProperty("Can be synced");
      tableRow.heimatNotes = new SimpleStringProperty("Heimat notes");
      tableRow.keeptimeNotes = new SimpleStringProperty("Current notes");
      tableRow.heimatTimeMinutes = new SimpleIntegerProperty(90);
      tableRow.userTimeMinutes = new SimpleIntegerProperty(90);
      tableRow.keeptimeTimeMinutes = new SimpleIntegerProperty(90);
      final ObservableList<TableRow> items = FXCollections.observableArrayList(tableRow);
      mappingTableView.setItems(items);

      ObservableList<TableRow> items2 = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[] { item.userTimeMinutes, item.shouldSyncCheckBox });
      items2.addAll(items);
      StringBinding totalSum = Bindings.createStringBinding(() -> localTimeStringConverter.toString(
            LocalTime.ofSecondOfDay(items.stream()
                                         .filter(item -> item.shouldSyncCheckBox.get())
                                         .mapToInt(item -> item.userTimeMinutes.getValue())
                                         .sum() * 60)), items2);
      sumTimeLabel.textProperty().bind(Bindings.concat("Total Sum: ", totalSum));

      TableColumn<TableRow, Boolean> shouldSyncColumn = new TableColumn<>("Sync");
      shouldSyncColumn.setCellValueFactory(data -> data.getValue().shouldSyncCheckBox);
      shouldSyncColumn.setCellFactory(CheckBoxTableCell.forTableColumn(shouldSyncColumn));
      mappingTableView.setEditable(true);
      shouldSyncColumn.setEditable(true);
      shouldSyncColumn.setPrefWidth(50);

      TableColumn<TableRow, String> projectColumn = new TableColumn<>("Project");
      projectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().project.getName()));
      projectColumn.setPrefWidth(100);
      TableColumn<TableRow, TableRow> timeColumn = new TableColumn<>("Time");
      timeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      timeColumn.setCellFactory(column -> new TableCell<>() {
         private final Spinner<LocalTime> timeSpinner = new Spinner<>();
         private final Label keeptimeLabel = new Label();
         private final Label heimatLabel = new Label();

         private final VBox container = new VBox(5); // Space between TextArea and Label

         {
            setUpTimeSpinner(timeSpinner);
            container.getChildren().addAll(timeSpinner, keeptimeLabel, heimatLabel);
         }

         @Override
         protected void updateItem(TableRow item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
               setGraphic(null);
            } else {
               keeptimeLabel.setText("KeepTime: " + localTimeStringConverter.toString(
                     LocalTime.ofSecondOfDay(item.keeptimeTimeMinutes.get() * 60)));
               heimatLabel.setText("Heimat: " + localTimeStringConverter.toString(
                     LocalTime.ofSecondOfDay(item.heimatTimeMinutes.get() * 60)));
               timeSpinner.getValueFactory().setValue(LocalTime.ofSecondOfDay(item.userTimeMinutes.get() * 60));
               timeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                  item.userTimeMinutes.set(newValue.getHour() * 60 + newValue.getMinute());
               });
               setGraphic(container);
            }
         }
      });
      timeColumn.setPrefWidth(125);

      TableColumn<TableRow, TableRow> notesColumn = new TableColumn<>("Notes");
      notesColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      notesColumn.setCellFactory(column -> new TableCell<>() {
         private final TextArea textArea = new TextArea();
         private final Label label = new Label();
         private final VBox container = new VBox(5); // Space between TextArea and Label

         {
            textArea.setPrefHeight(50);
            textArea.setPrefWidth(100);
            textArea.setWrapText(true);
            container.getChildren().addAll(textArea, label);
         }

         @Override
         protected void updateItem(TableRow item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
               setGraphic(null);
            } else {
               textArea.setText(item.keeptimeNotes.get());
               textArea.textProperty().addListener((obs, oldText, newText) -> item.keeptimeNotes.set(newText));
               label.setText(item.heimatNotes.get());
               setGraphic(container);
            }
         }
      });
      notesColumn.setPrefWidth(250);

      TableColumn<TableRow, String> syncColumn = new TableColumn<>("Sync Status");
      syncColumn.setCellValueFactory(data -> data.getValue().syncStatus);
      syncColumn.setPrefWidth(100);
      mappingTableView.getColumns().addAll(shouldSyncColumn, projectColumn, timeColumn, notesColumn, syncColumn);

      saveButton.setOnAction((ae) -> {
         LOG.debug("New mappings to be synced '{}'.", "TODO");
         // TODO close
      });

      cancelButton.setOnAction(ae -> {
         // TODO Close
      });
   }

   private void setUpTimeSpinner(final Spinner<LocalTime> spinner) {
      spinner.focusedProperty().addListener((e) -> {
         final LocalTimeStringConverter stringConverter = new LocalTimeStringConverter(FormatStyle.MEDIUM);
         final StringProperty text = spinner.getEditor().textProperty();
         try {
            stringConverter.fromString(text.get());
            // needed to log in value from editor to spinner
            spinner.increment(0); // TODO find better Solution
         } catch (final DateTimeParseException ex) {
            text.setValue(spinner.getValue().toString());
         }
      });

      spinner.setValueFactory(new SpinnerValueFactory<LocalTime>() {

         @Override
         public void decrement(final int steps) {
            if (getValue() == null) {
               setValue(LocalTime.now());
            } else {
               final LocalTime time = getValue();
               setValue(time.minusMinutes(15 * steps));
            }

         }

         @Override
         public void increment(final int steps) {
            if (getValue() == null) {
               setValue(LocalTime.now());
            } else {
               final LocalTime time = getValue();
               setValue(time.plusMinutes(15 * steps));
            }

         }

      });

      spinner.getValueFactory().setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

   }

   class TableRow {
      public BooleanProperty shouldSyncCheckBox;
      public Project project;

      public StringProperty keeptimeNotes;
      public StringProperty userNotes;
      public StringProperty heimatNotes;

      public IntegerProperty keeptimeTimeMinutes;
      public IntegerProperty userTimeMinutes;
      public IntegerProperty heimatTimeMinutes;

      public StringProperty syncStatus;

   }
}
