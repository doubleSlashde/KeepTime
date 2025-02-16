package de.doubleslash.keeptime.view;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.*;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.model.settings.HeimatSettings;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTime;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class ExternalProjectsSyncController {

   private static final Logger LOG = LoggerFactory.getLogger(ExternalProjectsSyncController.class);

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

   private final Controller controller;
   private final Model model;
   private final HeimatSettings heimatSettings;
   private final ExternalProjectsMappingsRepository externalProjectsMappingsRepository;

   private final LocalTimeStringConverter localTimeStringConverter = new LocalTimeStringConverter(FormatStyle.MEDIUM);
   private ObservableList<TableRow> items;
   private final HeimatAPI heimatAPI;
   private LocalDate currentReportDate;
   private Stage thisStage;

   public ExternalProjectsSyncController(final Controller controller, final Model model, HeimatSettings heimatSettings,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository) {
      this.controller = controller;
      this.model = model;
      this.heimatSettings = heimatSettings;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
      heimatAPI = new HeimatAPI(heimatSettings.getHeimatUrl(), heimatSettings.getHeimatPat());
   }

   public void initForDate(LocalDate currentReportDate, List<Work> currentWorkItems) {
      dayOfSyncLabel.setText(currentReportDate.format(DateTimeFormatter.BASIC_ISO_DATE));
      this.currentReportDate = currentReportDate;
      // TODO check if external projects are available for the currentDay
      // final List<HeimatTask> heimatTasks = heimatAPI.getMyTasks(currentReportDate);
      // TODO add a spinner while loading?
      final List<HeimatTime> heimatTimes = heimatAPI.getMyTimes(currentReportDate);
      final List<ExternalProjectMapping> mappedProjects = externalProjectsMappingsRepository.findByExternalSystemId(
            ExternalSystem.Heimat);

      final List<TableRow> list = new ArrayList<>();

      final SortedSet<Project> workedProjectsSet = currentWorkItems.stream()
                                                                   .map(Work::getProject)
                                                                   .filter(Project::isWork)
                                                                   .collect(Collectors.toCollection(() -> new TreeSet<>(
                                                                         Comparator.comparing(Project::getIndex))));
      for (final Project project : workedProjectsSet) {
         String heimatNotes = "";
         long heimatTimeSeconds = 0;
         boolean isMappedInHeimat = false;
         final Optional<ExternalProjectMapping> optionalHeimatMapping = mappedProjects.stream()
                                                                                      .filter(mp -> mp.getProject()
                                                                                                      .getId()
                                                                                            == project.getId())
                                                                                      .findAny();
         Optional<HeimatTime> optionalAlreadyBookedTime = Optional.empty();
         if (optionalHeimatMapping.isPresent()) {
            isMappedInHeimat = true;
            // TODO possibly there is more than one already booked time!
            // TODO there might be more than one KeepTime project assigned to HEIMAT project
            optionalAlreadyBookedTime = heimatTimes.stream()
                                                   .filter(heimatTime -> heimatTime.taskId()
                                                         == optionalHeimatMapping.get().getExternalTaskId())
                                                   .findAny();
            if (optionalAlreadyBookedTime.isPresent()) {
               heimatNotes = optionalAlreadyBookedTime.get().note();
               heimatTimeSeconds = optionalAlreadyBookedTime.get().durationInMinutes() * 60L;
            }
         }
         final List<Work> onlyCurrentProjectWork = currentWorkItems.stream()
                                                                   .filter(w -> w.getProject() == project)
                                                                   .toList();

         final long projectWorkSeconds = controller.calcSeconds(onlyCurrentProjectWork);

         final ProjectReport pr = new ProjectReport();
         for (final Work work : onlyCurrentProjectWork) {
            final String currentWorkNote = work.getNotes();
            pr.appendToWorkNotes(currentWorkNote);
         }
         final String keeptimeNotes = pr.getNotes();
         String canBeSynced = "Can be synced";
         if (!isMappedInHeimat) {
            canBeSynced = "Not mapped in Heimat";
         }
         list.add(new TableRow(project, isMappedInHeimat ? optionalHeimatMapping.get().getExternalTaskId() : -1,
               optionalAlreadyBookedTime.orElse(null), isMappedInHeimat, canBeSynced, heimatNotes, keeptimeNotes,
               keeptimeNotes, heimatTimeSeconds, projectWorkSeconds, projectWorkSeconds));
      }
      items = FXCollections.observableArrayList(list);
      mappingTableView.setItems(items);

      ObservableList<TableRow> items2 = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[] { item.userTimeSeconds, item.shouldSyncCheckBox });
      items2.addAll(items);
      StringBinding totalSum = Bindings.createStringBinding(() -> localTimeStringConverter.toString(
            LocalTime.ofSecondOfDay(items.stream()
                                         .filter(item -> item.shouldSyncCheckBox.get())
                                         .mapToLong(item -> item.userTimeSeconds.getValue())
                                         .sum())), items2);
      sumTimeLabel.textProperty().bind(Bindings.concat("Total Sum: ", totalSum));
      // TODO add a label with current Heimat Time
      // TODO add a label with current Keeptime time
   }

   @FXML
   private void initialize() {
      TableColumn<TableRow, Boolean> shouldSyncColumn = new TableColumn<>("Sync");
      shouldSyncColumn.setCellValueFactory(data -> data.getValue().shouldSyncCheckBox);
      shouldSyncColumn.setCellFactory(CheckBoxTableCell.forTableColumn(shouldSyncColumn));
      mappingTableView.setEditable(true);
      shouldSyncColumn.setEditable(true);
      shouldSyncColumn.setPrefWidth(50);

      TableColumn<TableRow, Project> projectColumn = new TableColumn<>("Project");
      projectColumn.setCellValueFactory(data -> new SimpleObjectProperty(data.getValue().project));
      projectColumn.setCellFactory(column -> new TableCell<>() {
         //private final Label label = new Label();

         @Override
         protected void updateItem(Project item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
               setGraphic(null);
               setText(null);
            } else {
               // TODO add a reference to the Heimat project which will be used
               //label.setText(item.getName());
               setText(item.getName());
               final Circle circle = new Circle(6, item.getColor());
               this.setGraphic(circle);
            }
         }
      });
      projectColumn.setPrefWidth(100);

      TableColumn<TableRow, TableRow> timeColumn = new TableColumn<>("Time");
      timeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      Consumer<Spinner<LocalTime>> spinnerValid = (Spinner<LocalTime> spinner) -> {
         int seconds = spinner.getValue().toSecondOfDay();
         int minutes = (seconds / 60);
         if (seconds != 0 || minutes % 15 != 0 || minutes <= 0) {
            spinner.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 4px;");
         } else {
            spinner.setStyle(""); // Reset to default style
         }
      };
      timeColumn.setCellFactory(column -> new TableCell<>() {
         private final Spinner<LocalTime> timeSpinner = new Spinner<>();
         private final Label keeptimeLabel = new Label();
         private final Label heimatLabel = new Label();
         private ChangeListener<LocalTime> localTimeChangeListener;
         private final VBox container = new VBox(5); // Space between TextArea and Label

         {
            setUpTimeSpinner(timeSpinner);
            container.getChildren().addAll(timeSpinner, keeptimeLabel, heimatLabel);
         }

         @Override
         protected void updateItem(TableRow item, boolean empty) {
            super.updateItem(item, empty);
            if (localTimeChangeListener != null)
               timeSpinner.valueProperty().removeListener(localTimeChangeListener);
            if (empty || item == null) {
               setGraphic(null);
            } else {
               keeptimeLabel.setText("KeepTime: " + localTimeStringConverter.toString(
                     LocalTime.ofSecondOfDay(item.keeptimeTimeSeconds.get())));
               heimatLabel.setText("Heimat: " + localTimeStringConverter.toString(
                     LocalTime.ofSecondOfDay(item.heimatTimeSeconds.get())));
               timeSpinner.getValueFactory().setValue(LocalTime.ofSecondOfDay(item.userTimeSeconds.get()));
               localTimeChangeListener = (observable, oldValue, newValue) -> {
                  item.userTimeSeconds.set(newValue.toSecondOfDay());
                  spinnerValid.accept(timeSpinner);
               };
               spinnerValid.accept(timeSpinner);
               timeSpinner.valueProperty().addListener(localTimeChangeListener);
               setGraphic(container);
            }
         }
      });
      timeColumn.setPrefWidth(125);

      TableColumn<TableRow, TableRow> notesColumn = new TableColumn<>("Notes");
      notesColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      Consumer<TextArea> textAreaValid = (TextArea textArea) -> {
         if (textArea.getText().isBlank()) {
            textArea.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 4px;");
         } else {
            textArea.setStyle(""); // Reset to default style
         }
      };
      notesColumn.setCellFactory(column -> new TableCell<>() {
         private ChangeListener<String> stringChangeListener;
         private final TextArea textArea = new TextArea();
         private final Label heimatNotesLabel = new Label();
         private final HBox hbox = new HBox(5);
         private final VBox container = new VBox(5); // Space between TextArea and Label

         {
            textArea.setPrefHeight(50);
            textArea.setPrefWidth(100);
            textArea.setWrapText(true);
            // TODO make it possible to copy content of heimatNotesLabel
            hbox.getChildren().addAll(new Label("Heimat:"), heimatNotesLabel);
            container.getChildren().addAll(textArea, hbox);
         }

         @Override
         protected void updateItem(TableRow item, boolean empty) {
            super.updateItem(item, empty);
            if (stringChangeListener != null)
               textArea.textProperty().removeListener(stringChangeListener);
            if (empty || item == null) {
               setGraphic(null);
            } else {
               textArea.setText(item.keeptimeNotes.get());
               stringChangeListener = (obs, oldText, newText) -> {
                  item.userNotes.set(newText);
                  textAreaValid.accept(textArea);
               };
               textAreaValid.accept(textArea);
               textArea.textProperty().addListener(stringChangeListener);
               heimatNotesLabel.setText(item.heimatNotes.get());
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

         // TODO show progress
         items.stream().filter(tr -> tr.shouldSyncCheckBox.get()).forEach(tr -> {
            if (tr.userNotes.get().isEmpty()) {
               LOG.warn("No notes were given. Skipping '{}'", tr.project.getName());
               return;
            }
            final int durationInMinutes = Math.toIntExact(tr.userTimeSeconds.get() / 60);
            if (durationInMinutes <= 0 || durationInMinutes % 15 != 0) {
               LOG.warn("Duration '{}' is not valid for project '{}'.", durationInMinutes, tr.project.getName());
               return;
            }

            final HeimatTime heimatTime = new HeimatTime(tr.heimatTaskId, currentReportDate, null, null,
                  durationInMinutes, tr.userNotes.get(), 0L);

            try {
               if (tr.optionalExistingTime != null) {
                  LOG.info("Removing existing booked time '{}'", tr.optionalExistingTime);
                  heimatAPI.deleteMyTime(tr.optionalExistingTime.id());
               }
               LOG.info("Adding new time time '{}'", heimatTime);
               heimatAPI.addMyTime(heimatTime);
            } catch (Exception e) {
               // TODO show errors to the user
               LOG.error("Error while persisting time '{}'", heimatTime, e);
            }
         });

         thisStage.close();
      });

      cancelButton.setOnAction(ae -> {
         thisStage.close();
      });

      // TODO offer some way to book time to an additional project?
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
               if (steps == 0)
                  return;
               final LocalTime time = getValue();
               setValue(decrementToLastFullQuarter(time));
            }

         }

         @Override
         public void increment(final int steps) {
            if (getValue() == null) {
               setValue(LocalTime.now());
            } else {
               if (steps == 0)
                  return;
               final LocalTime time = getValue();
               setValue(incrementToNextFullQuarter(time));
            }

         }

      });

      spinner.getValueFactory().setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

      // TODO mark red if not a 15 minute slot
   }

   public static LocalTime decrementToLastFullQuarter(LocalTime time) {
      int minutes = time.getMinute();
      if (minutes == 0)
         return time; // don't decrement below 0
      int decrement = (minutes % 15 == 0 && time.getSecond() == 0) ? 15 : minutes % 15;
      return time.minusMinutes(decrement).withSecond(0).withNano(0);
   }

   public static LocalTime incrementToNextFullQuarter(LocalTime time) {
      int minutes = time.getMinute();
      int increment = (minutes % 15 == 0 && time.getSecond() == 0) ? 15 : 15 - (minutes % 15);
      return time.plusMinutes(increment).withSecond(0).withNano(0);
   }

   public void setStage(final Stage thisStage) {
      this.thisStage = thisStage;
   }

   static class TableRow {
      private final long heimatTaskId;
      private final HeimatTime optionalExistingTime;
      public BooleanProperty shouldSyncCheckBox;
      public Project project;

      public StringProperty keeptimeNotes;
      public StringProperty userNotes;
      public StringProperty heimatNotes;

      public LongProperty keeptimeTimeSeconds;
      public LongProperty userTimeSeconds;
      public LongProperty heimatTimeSeconds;

      public StringProperty syncStatus;

      public TableRow(final Project project, long heimatTaskId, HeimatTime optionalExistingTime,
            final boolean shouldSync, final String syncStatus, final String heimatNotes, final String keeptimeNotes,
            String userNotes, final long heimatTimeSeconds, final long keeptimeSeconds, final long userSeconds) {
         this.project = project;
         this.heimatTaskId = heimatTaskId;
         this.optionalExistingTime = optionalExistingTime;
         this.shouldSyncCheckBox = new SimpleBooleanProperty(shouldSync);
         this.syncStatus = new SimpleStringProperty(syncStatus);
         this.heimatNotes = new SimpleStringProperty(heimatNotes);
         this.keeptimeNotes = new SimpleStringProperty(keeptimeNotes);
         this.userNotes = new SimpleStringProperty(userNotes);
         this.heimatTimeSeconds = new SimpleLongProperty(heimatTimeSeconds);
         this.userTimeSeconds = new SimpleLongProperty(userSeconds);
         this.keeptimeTimeSeconds = new SimpleLongProperty(keeptimeSeconds);
      }
   }
}
