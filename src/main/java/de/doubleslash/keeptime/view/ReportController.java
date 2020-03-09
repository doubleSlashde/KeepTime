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

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.scene.control.skin.DatePickerSkin;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.FontProvider;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ReportController {

   public static final String NOTE_DELIMETER = "; ";

   public static final String EMPTY_NOTE = "- No notes -";

   private static final String FX_BACKGROUND_COLOR_NOT_WORKED = "-fx-background-color: #BBBBBB;";

   @FXML
   private BorderPane topBorderPane;

   @FXML
   private Label currentDayLabel;
   @FXML
   private Label currentDayWorkTimeLabel;
   @FXML
   private Label currentDayTimeLabel;

   @FXML
   private GridPane gridPane;

   @FXML
   private ScrollPane scrollPane;

   @FXML
   private AnchorPane reportRoot;

   @FXML
   private Canvas colorTimeLineCanvas;

   private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

   private DatePicker datePicker; // for calendar element

   private Model model;

   private Controller controller;

   private Stage stage;

   private ColorTimeLine colorTimeLine;

   private LocalDate currentReportDate;

   @FXML
   private void initialize() {
      LOG.info("Init reportController");
      currentReportDate = LocalDate.now();

      colorTimeLine = new ColorTimeLine(colorTimeLineCanvas);
   }

   private void updateReport(final LocalDate dateToShow) {
      this.currentReportDate = dateToShow;
      this.loadCalenderWidget();
      reportRoot.requestFocus();

      this.currentDayLabel.setText(DateFormatter.toDayDateString(dateToShow));
      final List<Work> currentWorkItems = model.getWorkRepository().findByCreationDateOrderByStartTimeAsc(dateToShow);
      ;

      colorTimeLine.update(currentWorkItems, controller.calcSeconds(currentWorkItems));

      final SortedSet<Project> workedProjectsSet = currentWorkItems.stream().map(Work::getProject)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Project::getName))));

      this.gridPane.getChildren().clear();
      this.gridPane.getRowConstraints().clear();
      this.gridPane.getColumnConstraints().get(0).setPrefWidth(300);

      int rowIndex = 0;
      long currentWorkSeconds = 0;
      long currentSeconds = 0;

      for (final Project project : workedProjectsSet) {
         final Label projectName = new Label(project.getName());
         projectName.setFont(FontProvider.getBoldFont());
         projectName.setUnderline(project.isWork());
         final Circle circle = new Circle(5, project.getColor());

         final HBox projectNameHBox = new HBox();
         projectNameHBox.setAlignment(Pos.CENTER_LEFT);
         projectNameHBox.setPadding(new Insets(0, 0, 0, 5));
         projectNameHBox.setSpacing(5);

         projectNameHBox.getChildren().add(circle);
         projectNameHBox.getChildren().add(projectName);

         this.gridPane.add(projectNameHBox, 0, rowIndex);

         final List<Work> onlyCurrentProjectWork = currentWorkItems.stream().filter(w -> w.getProject() == project)
               .collect(Collectors.toList());

         final long todaysWorkSeconds = controller.calcSeconds(onlyCurrentProjectWork);

         currentSeconds += todaysWorkSeconds;
         if (project.isWork()) {
            currentWorkSeconds += todaysWorkSeconds;
         }

         final Label workedTimeLabel = new Label(DateFormatter.secondsToHHMMSS(todaysWorkSeconds));
         workedTimeLabel.setFont(FontProvider.getBoldFont());
         this.gridPane.add(workedTimeLabel, 2, rowIndex);

         // text will be set later
         final Button bProjectReport = createProjectReport();
         this.gridPane.add(bProjectReport, 1, rowIndex);

         rowIndex++;

         final ProjectReport pr = new ProjectReport(onlyCurrentProjectWork.size());
         for (int j = 0; j < onlyCurrentProjectWork.size(); j++) {
            final Work work = onlyCurrentProjectWork.get(j);
            final String workedHours = DateFormatter
                  .secondsToHHMMSS(DateFormatter.getSecondsBewtween(work.getStartTime(), work.getEndTime()));

            final String currentWorkNote = work.getNotes();
            pr.appendToWorkNotes(currentWorkNote);
            final Label commentLabel = new Label(currentWorkNote);
            commentLabel.setFont(FontProvider.getDefaultFont());
            commentLabel.setWrapText(true);
            this.gridPane.add(commentLabel, 0, rowIndex);

            final Label fromTillLabel = new Label(DateFormatter.toTimeString(work.getStartTime()) + " - "
                  + DateFormatter.toTimeString(work.getEndTime()));
            fromTillLabel.setFont(FontProvider.getDefaultFont());
            fromTillLabel.setWrapText(true);
            this.gridPane.add(fromTillLabel, 1, rowIndex);

            final Label workedHoursLabel = new Label(workedHours);
            workedHoursLabel.setFont(FontProvider.getDefaultFont());
            this.gridPane.add(workedHoursLabel, 2, rowIndex);

            final HBox clickDummy = new HBox();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem editMenuItem = new MenuItem("edit");

            editMenuItem.setOnAction(e -> {
               LOG.info("Edit work");
               final Dialog<Work> dialog = setupEditWorkDialog("Edit work", "Edit work ", work);

               final Optional<Work> result = dialog.showAndWait();

               result.ifPresent(editedWork -> {
                  controller.editWork(work, editedWork);

                  this.update();
               });
            });

            contextMenu.getItems().add(editMenuItem);

            clickDummy.setOnContextMenuRequested(
                  event -> contextMenu.show(clickDummy, event.getScreenX(), event.getScreenY()));

            this.gridPane.add(clickDummy, 0, rowIndex, 3, 1);

            rowIndex++;
         }
         bProjectReport.setUserData(pr.getNotes(true));
      }
      this.scrollPane.setVvalue(0); // scroll to the top

      this.currentDayTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentSeconds));
      this.currentDayWorkTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentWorkSeconds));

   }

   private void loadCalenderWidget() {
      final DatePicker myDatePicker = new DatePicker(this.currentReportDate);
      myDatePicker.valueProperty().addListener((observable, oldvalue, newvalue) -> {
         LOG.info("Datepicker selected value changed to {}", newvalue);
         updateReport(newvalue);
      });

      // HACK to show calendar from datepicker
      // https://stackoverflow.com/questions/34681975/javafx-extract-calendar-popup-from-datepicker-only-show-popup
      final DatePickerSkin datePickerSkin = new DatePickerSkin(myDatePicker);
      final Callback<DatePicker, DateCell> dayCellFactory = callback -> new DateCell() {
         @Override
         public void updateItem(final LocalDate item, final boolean empty) {
            super.updateItem(item, empty);
            if (model.getWorkRepository().findByCreationDateOrderByStartTimeAsc(item).isEmpty()) {
               setDisable(true);
               setStyle(FX_BACKGROUND_COLOR_NOT_WORKED);
            }
         }

      };

      myDatePicker.setDayCellFactory(dayCellFactory);
      final Node popupContent = datePickerSkin.getPopupContent();
      this.topBorderPane.setRight(popupContent);

   }

   private Dialog<Work> setupEditWorkDialog(final String title, final String headerText, final Work work) {
      final Dialog<Work> dialog = new Dialog<>();

      dialog.initOwner(stage);
      dialog.setTitle(title);
      dialog.setHeaderText(headerText);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

      final GridPane grid = setUpEditWorkGridPane(work, dialog);
      dialog.getDialogPane().setContent(grid);

      return dialog;
   }

   private GridPane setUpEditWorkGridPane(final Work work, final Dialog<Work> dialog) {
      final GridPane grid;
      final FXMLLoader loader = new FXMLLoader(Resources.getResource(RESOURCE.FXML_MANAGE_WORK));
      try {
         grid = loader.load();
      } catch (final IOException e) {
         throw new FXMLLoaderException("Error while loading '" + Resources.RESOURCE.FXML_MANAGE_WORK + "'.", e);
      }
      final ManageWorkController manageWorkController = loader.getController();
      manageWorkController.setModel(model);
      manageWorkController.initializeWith(work);

      dialog.setResultConverter(dialogButton -> {
         if (dialogButton == ButtonType.OK) {
            return manageWorkController.getWorkFromUserInput();
         }
         // TODO: Do you really want to return null?
         return null;
      });

      return grid;
   }

   private Button createProjectReport() {
      final Button bProjectReport = new Button("Copy to clipboard");
      final EventHandler<ActionEvent> eventListener = new EventHandler<ActionEvent>() {

         @Override
         public void handle(final ActionEvent event) {
            final Object source = event.getSource();
            final Button btn = (Button) source;
            final Object userData = btn.getUserData();
            final String notes = (String) userData;

            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(notes);
            clipboard.setContent(content);
         }

      };
      bProjectReport.setOnAction(eventListener);
      return bProjectReport;
   }

   public void setModel(final Model model) {
      this.model = model;

      this.loadCalenderWidget();
   }

   public void update() {
      updateReport(this.currentReportDate);
   }

   public void setController(final Controller controller) {
      this.controller = controller;
   }

   public void setStage(final Stage stage) {
      this.stage = stage;
   }
}
