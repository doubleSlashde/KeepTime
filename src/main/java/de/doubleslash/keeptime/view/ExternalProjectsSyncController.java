package de.doubleslash.keeptime.view;

import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.SvgNodeProvider;
import de.doubleslash.keeptime.controller.HeimatController;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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

   @FXML
   private VBox loadingScreen;

   @FXML
   private AnchorPane pane;

   @FXML
   private Label loadingMessage;

   private final SVGPath loadingSpinner = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_SPINNER_SOLID, 0.1, 0.1);
   private final SVGPath loadingSuccess = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_THUMBS_UP_SOLID, 0.1, 0.1);
   private final SVGPath loadingFailure = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_XMARK_SOLID, 0.1, 0.1);

   private final LocalTimeStringConverter localTimeStringConverter = new LocalTimeStringConverter(FormatStyle.MEDIUM);
   private ObservableList<TableRow> items;

   private LocalDate currentReportDate;
   private Stage thisStage;
   private final  HeimatController heimatController;

   public ExternalProjectsSyncController(final HeimatController heimatController) {
      this.heimatController = heimatController;
   }

   public void initForDate(LocalDate currentReportDate, List<Work> currentWorkItems) {
      dayOfSyncLabel.setText(currentReportDate.format(DateTimeFormatter.BASIC_ISO_DATE));
      this.currentReportDate = currentReportDate;

      // TODO add a spinner while loading?
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(currentReportDate,
            currentWorkItems);

      items = FXCollections.observableArrayList(tableRows.stream().map(mapping -> {
         String userNotes = mapping.keeptimeNotes();
         long userSeconds = mapping.keeptimeSeconds();
         // use info from heimat
         if (mapping.heimatSeconds() != 0L) {
            userNotes = mapping.heimatNotes();
            userSeconds = mapping.heimatSeconds();
         }
         return new TableRow(mapping, userNotes, userSeconds);
      }).toList());

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
      initializeLoadingScreen();

      TableColumn<TableRow, TableRow> shouldSyncColumn = new TableColumn<>("Sync");
      shouldSyncColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
      // Custom Cell Factory to disable CheckBoxes
      shouldSyncColumn.setCellFactory(col -> new TableCell<TableRow, TableRow>() {
         private final CheckBox checkBox = new CheckBox();
         private ChangeListener<Boolean> boolChangeListener;

         @Override
         protected void updateItem(TableRow item, boolean empty) {
            super.updateItem(item, empty);
            if (boolChangeListener != null)
               checkBox.selectedProperty().removeListener(boolChangeListener);

            if (empty || item == null) {
               setGraphic(null);
            } else {
               checkBox.setDisable(!item.mapping.canBeSynced());
               checkBox.setSelected(item.shouldSyncCheckBox.get());
               boolChangeListener = (obs, oldText, newBoolean) -> {
                  item.shouldSyncCheckBox.set(newBoolean);
               };
               checkBox.selectedProperty().addListener(boolChangeListener);

               setGraphic(checkBox);
            }
         }
      });
      mappingTableView.setEditable(true);
      shouldSyncColumn.setEditable(true);
      shouldSyncColumn.setPrefWidth(50);

      TableColumn<TableRow, List<Project>> projectColumn = new TableColumn<>("Project");
      projectColumn.setCellValueFactory(data -> new SimpleObjectProperty(data.getValue().mapping.projects()));
      projectColumn.setCellFactory(column -> new TableCell<>() {
         //private final Label label = new Label();

         @Override
         protected void updateItem(List<Project> item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
               setGraphic(null);
               setText(null);
            } else {
               VBox vbox = new VBox(5);
               item.forEach(project -> {
                  vbox.getChildren().add(createRow(project.getColor(), project.getName()));
               });
               setGraphic(vbox);
            }
         }

         private HBox createRow(Color color, String text) {
            Circle circle = new Circle(6, color);
            Label label = new Label(text);

            return new HBox(5, circle, label);
         }
      });
      projectColumn.setPrefWidth(100);

      TableColumn<TableRow, TableRow> timeColumn = new TableColumn<>("Time");
      timeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      Consumer<Spinner<LocalTime>> spinnerValid = (Spinner<LocalTime> spinner) -> {
         int seconds = spinner.getValue().toSecondOfDay();
         int minutes = (seconds / 60);
         if (seconds % 60 != 0 || minutes % 15 != 0 || minutes <= 0) {
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
         private final Label keepTimeNotesLabel = new Label();
         private final HBox hbox = new HBox(5);
         private final HBox hbox2 = new HBox(5);
         private final VBox container = new VBox(5); // Space between TextArea and Label

         {
            textArea.setPrefHeight(50);
            textArea.setPrefWidth(100);
            textArea.setWrapText(true);
            // TODO make it possible to copy content of heimatNotesLabel
            hbox.getChildren().addAll(new Label("Keeptime:"), keepTimeNotesLabel);
            hbox2.getChildren().addAll(new Label("Heimat:"), heimatNotesLabel);
            container.getChildren().addAll(textArea, hbox, hbox2);
         }

         @Override
         protected void updateItem(TableRow item, boolean empty) {
            super.updateItem(item, empty);
            if (stringChangeListener != null)
               textArea.textProperty().removeListener(stringChangeListener);
            if (empty || item == null) {
               setGraphic(null);
            } else {
               textArea.setText(item.userNotes.get());
               stringChangeListener = (obs, oldText, newText) -> {
                  item.userNotes.set(newText);
                  textAreaValid.accept(textArea);
               };
               textAreaValid.accept(textArea);
               textArea.textProperty().addListener(stringChangeListener);
               heimatNotesLabel.setText(item.heimatNotes.get());
               keepTimeNotesLabel.setText(item.keeptimeNotes.get());
               setGraphic(container);
            }
         }
      });
      notesColumn.setPrefWidth(350);

      TableColumn<TableRow, String> syncColumn = new TableColumn<>("Sync Status");
      syncColumn.setCellValueFactory(data -> data.getValue().syncStatus);
      syncColumn.setPrefWidth(250);
      mappingTableView.getColumns().addAll(shouldSyncColumn, projectColumn, timeColumn, notesColumn, syncColumn);

      saveButton.setOnAction((ae) -> {
         LOG.debug("New mappings to be synced '{}'.", "TODO");
         showLoadingScreen(true);

         Task<List<HeimatController.HeimatErrors>> task = new Task<>() {
            @Override
            protected List<HeimatController.HeimatErrors> call() {
               return heimatController.saveDay(items.stream()
                                                    .map(item -> new HeimatController.Asdf(item.mapping,
                                                          item.shouldSyncCheckBox.get(), item.userNotes.get(),
                                                          (int) (item.userTimeSeconds.get() / 60)))
                                                    .toList(), currentReportDate);
            }
         };

         task.setOnSucceeded(e -> {
            LOG.error("Task successfull");
            final List<HeimatController.HeimatErrors> errors = task.getValue();
            if (!errors.isEmpty()) {
               loadingScreenShowSyncing("Something did not work :(", loadingFailure);
               List<String> a = errors.stream().map(error -> {
                  return error.mapping().getMapping().heimatTaskId() + ": " + error.errorMessage()
                        + ". Wanted to store '" + error.mapping().getUserMinutes() + "' minutes with notes '"
                        + error.mapping().getUserNotes() + "'";
               }).toList();

               showErrorDialog(a);
            } else {
               loadingScreenShowSyncing("Successfully synced!", loadingSuccess);
            }

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> {
               showLoadingScreen(false);
               thisStage.close();
            });
            delay.play();
         });

         task.setOnFailed(e -> {
            final Throwable exception = task.getException();
            LOG.error("Task failed", exception);
            loadingScreenShowSyncing("Something did not work :(", loadingFailure);

            showErrorDialog(Collections.singletonList("ERROR" + exception.getMessage()));
            showLoadingScreen(false);
            thisStage.close();
         });
         loadingScreenShowSyncing("Syncing...", loadingSpinner);
         Platform.runLater(() -> new Thread(task).start());
      });

      cancelButton.setOnAction(ae -> {
         thisStage.close();
      });

      // TODO offer some way to book time to an additional project?
   }

   private void initializeLoadingScreen() {
      loadingScreen.getChildren().add(0, loadingSpinner);

      loadingSuccess.setFill(Color.GREEN);
      loadingSuccess.prefHeight(50);
      loadingSuccess.prefWidth(50);

      loadingFailure.setFill(Color.RED);
      loadingFailure.prefHeight(50);
      loadingFailure.prefWidth(50);

      loadingSpinner.prefHeight(50);
      loadingSpinner.prefWidth(50);

      RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), loadingSpinner);
      rotateTransition.setByAngle(360);
      rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
      rotateTransition.play();
   }

   private void loadingScreenShowSyncing(String statusMessage, SVGPath icon) {
      final ObservableList<Node> children = loadingScreen.getChildren();
      children.remove(0);
      children.add(0, icon);
      loadingMessage.setText(statusMessage);
   }

   private void showLoadingScreen(final boolean show) {
      pane.setDisable(show);
      loadingScreen.setVisible(show);
      pane.setEffect(show ? new GaussianBlur() : null);
   }

   private void showErrorDialog(List<String> errorMessages) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error Details");
      alert.setHeaderText("The following errors occurred. Please copy and manually book the times.");
      alert.initOwner(thisStage);

      String errorText = String.join("\n\n", errorMessages);

      // Create a TextArea to allow copying
      TextArea textArea = new TextArea(errorText);
      textArea.setEditable(false); // Prevent editing
      textArea.setWrapText(true);  // Wrap long lines
      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);

      ScrollPane scrollPane = new ScrollPane(textArea);
      scrollPane.setFitToWidth(true);
      scrollPane.setFitToHeight(true);
      scrollPane.setPrefHeight(150);

      alert.getDialogPane().setContent(scrollPane);

      alert.getButtonTypes().setAll(ButtonType.OK);
      alert.showAndWait();
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
   }

   public static LocalTime decrementToLastFullQuarter(LocalTime time) {
      int minutes = time.getMinute();
      if (time.toSecondOfDay() == 0)
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

   public static class TableRow {
      private final HeimatController.Mapping mapping;

      public final BooleanProperty shouldSyncCheckBox;
      public final StringProperty keeptimeNotes;
      public final StringProperty userNotes;
      public final StringProperty heimatNotes;

      public final LongProperty keeptimeTimeSeconds;
      public final LongProperty userTimeSeconds;
      public final LongProperty heimatTimeSeconds;

      public final StringProperty syncStatus;

      public TableRow(HeimatController.Mapping mapping, String userNotes, final long userSeconds) {
         this.mapping = mapping;
         this.shouldSyncCheckBox = new SimpleBooleanProperty(mapping.canBeSynced());
         this.syncStatus = new SimpleStringProperty(mapping.syncMessage());

         this.keeptimeNotes = new SimpleStringProperty(mapping.keeptimeNotes());
         this.keeptimeTimeSeconds = new SimpleLongProperty(mapping.keeptimeSeconds());

         this.heimatNotes = new SimpleStringProperty(mapping.heimatNotes());
         this.heimatTimeSeconds = new SimpleLongProperty(mapping.heimatSeconds());

         this.userNotes = new SimpleStringProperty(userNotes);
         this.userTimeSeconds = new SimpleLongProperty(userSeconds);
      }
   }
}
