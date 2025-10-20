// Copyright 2025 doubleSlash Net Business GmbH
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

import de.doubleslash.keeptime.common.BrowserHelper;
import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.SvgNodeProvider;
import de.doubleslash.keeptime.controller.HeimatController;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.StyledMessage;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import de.doubleslash.keeptime.viewpopup.SearchPopup;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.doubleslash.keeptime.view.ReportController.copyToClipboard;

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
   private Label keepTimeTimeLabel;
   @FXML
   private Label heimatTimeLabel;

   @FXML
   private Hyperlink externalSystemLink;
   @FXML
   private Hyperlink externalSystemLinkLoadingScreen;

   @FXML
   private VBox loadingScreen;

   @FXML
   private AnchorPane pane;

   @FXML
   private Label loadingMessage;
   @FXML
   private Label loadingClosingMessage;
   @FXML
   private Region syncingIconRegion;

   @FXML
   private HBox heimatTaskSearchContainer;

   private final SVGPath loadingSpinner = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_SPINNER_SOLID, 0.1,
         0.1);
   private final SVGPath loadingSuccess = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_THUMBS_UP_SOLID,
         0.1, 0.1);
   private final SVGPath loadingFailure = SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_XMARK_SOLID, 0.1,
         0.1);
   private final Color colorLoadingSpinner = Color.valueOf("#00A5E1");
   private final Color colorLoadingSuccess = Color.valueOf("#74a317");
   private final Color colorLoadingFailure = Color.valueOf("#c63329");

   private final LocalTimeStringConverter localTimeStringConverter = new LocalTimeStringConverter(FormatStyle.MEDIUM);

   private SearchPopup<HeimatTask> heimatTaskSearchPopup;
   private ObservableList<TableRow> items;
   private ObservableList<TableRow> itemsForBindings;

   private LocalDate currentReportDate;
   private Stage thisStage;
   private final HeimatController heimatController;
   private final RotateTransition loadingSpinnerAnimation = new RotateTransition(Duration.seconds(1),
         syncingIconRegion);

   public ExternalProjectsSyncController(final HeimatController heimatController) {
      this.heimatController = heimatController;
   }

   public void initForDate(LocalDate currentReportDate, List<Work> currentWorkItems) {
      dayOfSyncLabel.setText(DateFormatter.toDayDateString(currentReportDate));
      this.currentReportDate = currentReportDate;

      // TODO add a spinner while loading?
      final List<HeimatController.Mapping> tableRows = heimatController.getTableRows(currentReportDate,
            currentWorkItems);

      items = FXCollections.observableArrayList(tableRows.stream().map(mapping -> {
         String userNotes = mapping.keeptimeNotes();
         long userSeconds = mapping.keeptimeSeconds();
         // use info from heimat when already present
         if (mapping.heimatSeconds() != 0L) {
            userNotes = mapping.heimatNotes();
            userSeconds = mapping.heimatSeconds();
         }
         return new TableRow(mapping, userNotes, userSeconds);
      }).toList());

      mappingTableView.setItems(items);

      itemsForBindings = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[] { item.userTimeSeconds, item.shouldSyncCheckBox, item.userNotes });
      itemsForBindings.addAll(items);
      StringBinding totalSum = Bindings.createStringBinding(() -> localTimeStringConverter.toString(
            LocalTime.ofSecondOfDay(
                  items.stream().filter(item -> item.mapping.heimatTaskId() != -1L) // if its bookable in heimat
                       .mapToLong(item -> {
                          if (item.shouldSyncCheckBox.get())
                             return item.userTimeSeconds.getValue();
                          else
                             return item.heimatTimeSeconds.get();
                       }).sum())), itemsForBindings);
      sumTimeLabel.textProperty().bind(totalSum);

      keepTimeTimeLabel.setText(localTimeStringConverter.toString(
            LocalTime.ofSecondOfDay(tableRows.stream().mapToLong(HeimatController.Mapping::keeptimeSeconds).sum())));
      heimatTimeLabel.setText(localTimeStringConverter.toString(
            LocalTime.ofSecondOfDay(tableRows.stream().mapToLong(HeimatController.Mapping::heimatSeconds).sum())));

      BooleanBinding projectsValidProperty = Bindings.createBooleanBinding(() -> items.stream().anyMatch(item -> {
         boolean shouldSync = item.shouldSyncCheckBox.get();
         boolean hasNote = !item.userNotes.get().isBlank();
         boolean hasTime = areSecondsOfDayValid(item.userTimeSeconds.get());
         return shouldSync && !(hasNote && hasTime);
      }), itemsForBindings);

      saveButton.disableProperty().bind(projectsValidProperty);
      externalSystemLink.setOnAction(ae -> BrowserHelper.openURL(heimatController.getUrlForDay(currentReportDate)));
      externalSystemLinkLoadingScreen.setOnAction(
            ae -> BrowserHelper.openURL(heimatController.getUrlForDay(currentReportDate)));

      final List<HeimatTask> tasksForDay = heimatController.getTasks(currentReportDate);

      final FilteredList<HeimatTask> tasksNotInList = new FilteredList<>(FXCollections.observableArrayList(tasksForDay),
            (task) -> items.stream().noneMatch(tr -> task.id() == tr.mapping.heimatTaskId()));
      items.addListener((ListChangeListener<? super TableRow>) c -> {
         final Predicate<? super HeimatTask> predicate = tasksNotInList.getPredicate();
         tasksNotInList.setPredicate(null);
         tasksNotInList.setPredicate(predicate);
      });

      heimatTaskSearchPopup = new SearchPopup<>(tasksNotInList);
      heimatTaskSearchPopup.setDisplayTextFunction(task -> task.taskHolderName() + " - " + task.name());

      heimatTaskSearchPopup.setOnItemSelected((selectedTask, popup) -> {
         if (selectedTask == null)
            return;
         boolean alreadyExists = items.stream().anyMatch(row -> row.mapping.heimatTaskId() == selectedTask.id());
         if (alreadyExists)
            return;

         StyledMessage syncMessage = StyledMessage.of(new StyledMessage.TextSegment("Manually added\n\nSync to "),
               new StyledMessage.TextSegment(selectedTask.name(), true),
               new StyledMessage.TextSegment("\n(" + selectedTask.taskHolderName() + ")"));

         TableRow addedRow = new TableRow(
               new HeimatController.Mapping(selectedTask.id(), true, true, syncMessage, "", List.of(), List.of(), "",
                     "", 0, 0), "", 0);
         items.add(addedRow);
         itemsForBindings.add(addedRow);
         mappingTableView.scrollTo(items.size() - 1);
      });
      heimatTaskSearchPopup.setClearFieldAfterSelection(true);

      heimatTaskSearchContainer.getChildren().add(heimatTaskSearchPopup.getComboBox());
      HBox.setHgrow(heimatTaskSearchPopup.getComboBox(), Priority.ALWAYS);
   }

   @FXML
   private void initialize() {
      initializeLoadingScreen();

      TableColumn<TableRow, TableRow> shouldSyncColumn = new TableColumn<>("Sync");
      shouldSyncColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
      // Custom Cell Factory to disable CheckBoxes
      shouldSyncColumn.setCellFactory(col -> new TableCell<>() {
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
               boolChangeListener = (obs, oldText, newBoolean) -> item.shouldSyncCheckBox.set(newBoolean);
               checkBox.selectedProperty().addListener(boolChangeListener);
               setAlignment(Pos.TOP_CENTER);
               setGraphic(checkBox);
            }
         }
      });
      mappingTableView.setEditable(true);
      shouldSyncColumn.setEditable(true);

      TableColumn<TableRow, List<Project>> projectColumn = new TableColumn<>("Project");
      projectColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().mapping.projects()));
      projectColumn.setCellFactory(column -> new TableCell<>() {
         @Override
         protected void updateItem(List<Project> item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
               setGraphic(null);
               setText(null);
            } else {
               VBox vbox = new VBox(5);

               for (Project project : item) {
                  HBox row = createRow(project.getColor(), project.getName());
                  vbox.getChildren().add(row);

                  // Set tooltip for the label
                  Label label = (Label) row.getChildren().get(1);
                  Tooltip tooltip = new Tooltip(label.getText());
                  label.setTooltip(tooltip);
               }
               setGraphic(vbox);
            }
         }

         private HBox createRow(Color color, String text) {
            Circle circle = new Circle(6, color);
            Label label = new Label(text);

            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);

            return new HBox(5, circle, label);
         }
      });

      TableColumn<TableRow, TableRow> timeColumn = new TableColumn<>("Time");
      timeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      Consumer<Spinner<LocalTime>> spinnerValidConsumer = (Spinner<LocalTime> spinner) -> {
         final boolean isValid = areSecondsOfDayValid(spinner.getValue().toSecondOfDay());
         markNodeValidOrNot(spinner, isValid);
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
               heimatLabel.setText("HEIMAT: " + localTimeStringConverter.toString(
                     LocalTime.ofSecondOfDay(item.heimatTimeSeconds.get())));
               timeSpinner.setDisable(!item.mapping.canBeSynced());
               timeSpinner.getValueFactory().setValue(LocalTime.ofSecondOfDay(0));
               if (item.mapping.canBeSynced()) {
                  timeSpinner.getValueFactory().setValue(LocalTime.ofSecondOfDay(item.userTimeSeconds.get()));
                  localTimeChangeListener = (observable, oldValue, newValue) -> {
                     item.userTimeSeconds.set(newValue.toSecondOfDay());
                     spinnerValidConsumer.accept(timeSpinner);
                  };
                  spinnerValidConsumer.accept(timeSpinner);
                  timeSpinner.valueProperty().addListener(localTimeChangeListener);
               }
               setGraphic(container);
            }
         }
      });

      TableColumn<TableRow, TableRow> notesColumn = new TableColumn<>("Notes");
      notesColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue())); // Placeholder property

      Consumer<TextArea> textAreaValid = (TextArea textArea) -> {
         final boolean isValid = !textArea.getText().isBlank();
         markNodeValidOrNot(textArea, isValid);
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

            final Button copyKeepTimeNotes = new Button("",
                  SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_CLIPBOARD_ICON, 0.03, 0.03));
            copyKeepTimeNotes.setMaxSize(20, 18);
            copyKeepTimeNotes.setMinSize(20, 18);
            copyKeepTimeNotes.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            copyKeepTimeNotes.setTooltip(new Tooltip("Copy notes"));
            copyKeepTimeNotes.setOnAction(me -> copyToClipboard(keepTimeNotesLabel.getText()));
            copyKeepTimeNotes.getStyleClass().add("tertiary-button");
            final Button copyHeimatNotes = new Button("",
                  SvgNodeProvider.getSvgNodeWithScale(Resources.RESOURCE.SVG_CLIPBOARD_ICON, 0.03, 0.03));
            copyHeimatNotes.setMaxSize(20, 18);
            copyHeimatNotes.setMinSize(20, 18);
            copyHeimatNotes.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            copyHeimatNotes.setTooltip(new Tooltip("Copy notes"));
            copyHeimatNotes.setOnAction(me -> copyToClipboard(heimatNotesLabel.getText()));
            copyHeimatNotes.getStyleClass().add("tertiary-button");

            final Label keeptimeLabel = new Label("KeepTime:");
            keeptimeLabel.setMinWidth(60);
            hbox.getChildren().addAll(copyKeepTimeNotes, keeptimeLabel, keepTimeNotesLabel);
            final Label heimatLabel = new Label("HEIMAT:");
            heimatLabel.setMinWidth(60);
            hbox2.getChildren().addAll(copyHeimatNotes, heimatLabel, heimatNotesLabel);
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
               textArea.setDisable(!item.mapping.canBeSynced());
               textArea.setText("");
               if (item.mapping.canBeSynced()) {
                  textArea.setText(item.userNotes.get());
                  stringChangeListener = (obs, oldText, newText) -> {
                     item.userNotes.set(newText);
                     textAreaValid.accept(textArea);
                  };
                  textAreaValid.accept(textArea);
                  textArea.textProperty().addListener(stringChangeListener);
               }
               heimatNotesLabel.setText(item.heimatNotes.get());
               keepTimeNotesLabel.setText(item.keeptimeNotes.get());
               setGraphic(container);
            }
         }
      });

      TableColumn<TableRow, TableRow> syncColumn = new TableColumn<>("Sync Status");
      syncColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
      syncColumn.setCellFactory(column -> new TableCell<>() {

         private final Tooltip tooltip = new Tooltip();

         @Override
         protected void updateItem(TableRow item, boolean empty) {

            super.updateItem(item, empty);

            if (empty || item == null) {
               setTooltip(null);
               setGraphic(null);
               return;
            }

            TextFlow statusFlow = item.syncStatus;
            String status = statusFlow.getChildren()
                                      .stream()
                                      .filter(n -> n instanceof Text)
                                      .map(n -> ((Text) n).getText())
                                      .collect(Collectors.joining());

            if (!item.bookingHint.isEmpty().get()) {
               statusFlow = new TextFlow(statusFlow);
               tooltip.setText(status + "\n" + item.bookingHint.get());
               Text icon = new Text("ⓘ ");
               icon.setStyle("-fx-text-fill: #1c2070; -fx-font-size: 14px;");
               statusFlow.getChildren().add(0, icon);
            } else {
               tooltip.setText(status);
            }

            // Fix Cell height not aligning with Textflow
            // https://stackoverflow.com/questions/42855724/textflow-inside-tablecell-not-correct-cell-height
            statusFlow.maxWidthProperty().bind(column.widthProperty());

            setGraphic(new Group(statusFlow));

            setTooltip(tooltip);
         }
      });

      shouldSyncColumn.setPrefWidth(50);
      projectColumn.setPrefWidth(100);
      timeColumn.setPrefWidth(125);
      notesColumn.prefWidthProperty().bind(mappingTableView.widthProperty().subtract(525 + 17));
      syncColumn.setPrefWidth(250);

      mappingTableView.getColumns().addAll(shouldSyncColumn, projectColumn, timeColumn, notesColumn, syncColumn);
      mappingTableView.setSelectionModel(null);
      mappingTableView.getColumns().forEach(column -> column.setSortable(false));

      saveButton.setOnAction(ae -> {
         showLoadingScreen(true);

         Task<List<HeimatController.HeimatErrors>> task = new Task<>() {
            @Override
            protected List<HeimatController.HeimatErrors> call() {
               return heimatController.saveDay(items.stream()
                                                    .map(item -> new HeimatController.UserMapping(item.mapping,
                                                          item.shouldSyncCheckBox.get(), item.userNotes.get(),
                                                          (int) (item.userTimeSeconds.get() / 60)))
                                                    .toList(), currentReportDate);
            }
         };

         task.setOnSucceeded(e -> {
            final List<HeimatController.HeimatErrors> errors = task.getValue();
            int closingSeconds = 5;
            if (!errors.isEmpty()) {
               closingSeconds = 10;
               loadingScreenShowSyncing("Something did not work :(", loadingFailure);
               List<String> a = errors.stream().map(error -> {
                  final List<Project> projects = error.mapping().mapping().projects();
                  // TODO would be nice to show heimat task name but we only have ID here
                  final String projectName = !projects.isEmpty()
                        ? projects.get(0).getName()
                        : Long.toString(error.mapping().mapping().heimatTaskId());
                  return projectName + ": " + error.errorMessage() + ". Wanted to store '" + error.mapping()
                                                                                                  .userMinutes()
                        + "' minutes with notes '" + error.mapping().userNotes() + "'";
               }).toList();

               showErrorDialog(a);
            } else {
               loadingScreenShowSyncing(
                     "Successfully synced!\nPlease always validate that everything worked like expected.",
                     loadingSuccess);
            }

            final AtomicInteger remainingSeconds = new AtomicInteger(closingSeconds);
            loadingClosingMessage.setText("Closing in " + remainingSeconds + " seconds...");
            loadingClosingMessage.setVisible(true);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
               remainingSeconds.getAndDecrement();
               loadingClosingMessage.setText("Closing in " + remainingSeconds + " seconds...");
               if (remainingSeconds.get() <= 0) {
                  showLoadingScreen(false);
                  thisStage.close();
                  loadingClosingMessage.setVisible(false);
               }
            }));
            timeline.setCycleCount(remainingSeconds.get());
            timeline.play();
         });

         task.setOnFailed(e -> {
            final Throwable exception = task.getException();
            LOG.error("Task failed unexpectedly.", exception);
            loadingScreenShowSyncing("Something very unexpected has happened :(", loadingFailure);

            showErrorDialog(Collections.singletonList(
                  "Please report this to a developer. The error was:" + exception.getMessage()));
            showLoadingScreen(false);
            thisStage.close();
         });
         loadingScreenShowSyncing("Syncing...", loadingSpinner);
         Platform.runLater(() -> new Thread(task).start());
      });

      cancelButton.setOnAction(ae -> {
         showLoadingScreen(false);
         thisStage.close();
      });
   }

   private static void markNodeValidOrNot(final Node textArea, final boolean isValid) {
      String borderColor = "#74a317";
      if (!isValid) {
         borderColor = "#c63329";
      }
      textArea.setStyle("-fx-border-color: " + borderColor + ";");
   }

   private static boolean areSecondsOfDayValid(final long seconds) {
      long minutes = (seconds / 60);
      final boolean isInvalid = seconds % 60 != 0 || minutes % 15 != 0 || minutes <= 0;
      return !isInvalid;
   }

   private void initializeLoadingScreen() {
      showLoadingScreen(false);
      loadingSuccess.setFill(colorLoadingSuccess);
      loadingSuccess.prefHeight(50);
      loadingSuccess.prefWidth(50);

      loadingFailure.setFill(colorLoadingFailure);
      loadingFailure.prefHeight(50);
      loadingFailure.prefWidth(50);

      loadingSpinner.setFill(colorLoadingSpinner);
      loadingSpinner.prefHeight(50);
      loadingSpinner.prefWidth(50);

      loadingSpinnerAnimation.setNode(syncingIconRegion);
      loadingSpinnerAnimation.setByAngle(360);
      loadingSpinnerAnimation.setCycleCount(Animation.INDEFINITE);
   }

   private void loadingScreenShowSyncing(String statusMessage, SVGPath icon) {
      if (icon == loadingSpinner) {
         loadingSpinnerAnimation.play();
      } else {
         loadingSpinnerAnimation.stop();
         syncingIconRegion.setRotate(0);
      }
      syncingIconRegion.setShape(icon);
      syncingIconRegion.setBackground(new Background(new BackgroundFill(icon.getFill(), null, null)));
      loadingMessage.setText(statusMessage);
   }

   private void showLoadingScreen(final boolean show) {
      if (!show)
         loadingSpinnerAnimation.stop();
      loadingClosingMessage.setVisible(false);
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

      BooleanProperty shiftDown = new SimpleBooleanProperty(false);

      spinner.sceneProperty().addListener((obs, oldScene, newScene) -> {
         if (newScene != null) {
            newScene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
               if (event.getCode() == KeyCode.SHIFT) {
                  shiftDown.set(false);
               }
            });

            newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
               if (event.getCode() == KeyCode.SHIFT) {
                  shiftDown.set(true);
               }
            });
         }
      });

      spinner.focusedProperty().addListener(e -> {
         final LocalTimeStringConverter stringConverter = new LocalTimeStringConverter(FormatStyle.MEDIUM);
         final StringProperty text = spinner.getEditor().textProperty();
         try {
            stringConverter.fromString(text.get());
            spinner.increment(0);
         } catch (final DateTimeParseException ex) {
            text.setValue(spinner.getValue().toString());
         }
      });

      spinner.setValueFactory(new SpinnerValueFactory<>() {

         @Override
         public void decrement(final int steps) {
            if (getValue() == null) {
               setValue(LocalTime.now());
            } else {
               if (steps == 0)
                  return;
               final LocalTime time = getValue();

               if (shiftDown.get())
                  setValue(decrementToNextHour(time));
               else
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

               if (shiftDown.get())
                  setValue(incrementToNextHour(time));
               else
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

   public static LocalTime incrementToNextHour(LocalTime time) {
      return time.plusHours(1).withMinute(0).withSecond(0).withNano(0);
   }

   public static LocalTime decrementToNextHour(LocalTime time) {
      if (time.getHour() == 0)
         return LocalTime.MIDNIGHT;

      return time.minusHours(1).withMinute(0).withSecond(0).withNano(0);
   }

   public void setStage(final Stage thisStage) {
      this.thisStage = thisStage;
   }

   /**
    * Converts a StyledMessage to a TextFlow for UI display.
    *
    * @param styledMessage
    *       The styled message to convert
    * @return A TextFlow with properly styled text segments
    */
   private static TextFlow convertStyledMessageToTextFlow(StyledMessage styledMessage) {
      TextFlow textFlow = new TextFlow();
      for (StyledMessage.TextSegment segment : styledMessage.getSegments()) {
         Text text = new Text(segment.text());
         if (segment.bold()) {
            text.setStyle("-fx-font-weight: bold;");
         }
         textFlow.getChildren().add(text);
      }
      return textFlow;
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

      public final TextFlow syncStatus;
      public final StringProperty bookingHint;

      public TableRow(HeimatController.Mapping mapping, String userNotes, final long userSeconds) {
         this.mapping = mapping;
         this.shouldSyncCheckBox = new SimpleBooleanProperty(mapping.shouldBeSynced());
         this.syncStatus = convertStyledMessageToTextFlow(mapping.syncMessage());
         this.bookingHint = new SimpleStringProperty(mapping.bookingHint());

         this.keeptimeNotes = new SimpleStringProperty(mapping.keeptimeNotes());
         this.keeptimeTimeSeconds = new SimpleLongProperty(mapping.keeptimeSeconds());

         this.heimatNotes = new SimpleStringProperty(mapping.heimatNotes());
         this.heimatTimeSeconds = new SimpleLongProperty(mapping.heimatSeconds());

         this.userNotes = new SimpleStringProperty(userNotes);
         this.userTimeSeconds = new SimpleLongProperty(userSeconds);
      }
   }
}
