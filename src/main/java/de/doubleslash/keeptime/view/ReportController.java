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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.javafx.scene.control.skin.DatePickerSkin;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.view.worktable.ProjectTableRow;
import de.doubleslash.keeptime.view.worktable.TableRow;
import de.doubleslash.keeptime.view.worktable.WorkTableRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;

@Component
public class ReportController {

   public static final String NOTE_DELIMETER = "; ";

   public static final String EMPTY_NOTE = "- No notes -";

   private static final String FX_BACKGROUND_COLOR_NOT_WORKED = "-fx-background-color: #BBBBBB;";

   private static final String FX_BACKGROUND_COLOR_WORKED = "-fx-background-color: #00a5e1;";

   private static final String EDIT_WORK_DIALOG_TITLE = "Edit work";

   @FXML
   private BorderPane topBorderPane;

   @FXML
   private Label currentDayLabel;
   @FXML
   private Label currentDayWorkTimeLabel;
   @FXML
   private Label currentDayTimeLabel;

   @FXML
   private TreeTableView<TableRow> workTableTreeView;

   @FXML
   private AnchorPane reportRoot;

   @FXML
   private Canvas colorTimeLineCanvas;

   private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

   private final Model model;

   private final Controller controller;

   private Stage stage;

   private ColorTimeLine colorTimeLine;

   private LocalDate currentReportDate;

   private final TreeItem<TableRow> rootItem = new TreeItem<>();

   @Autowired
   public ReportController(final Model model, final Controller controller) {
      this.model = model;
      this.controller = controller;
   }

   @FXML
   private void initialize() {
      LOG.info("Init reportController");
      currentReportDate = LocalDate.now();

      colorTimeLine = new ColorTimeLine(colorTimeLineCanvas);
      initTableView();
   }

   private void initTableView() {
      final TreeTableColumn<TableRow, TableRow> noteColumn = new TreeTableColumn<>("Notes");
      noteColumn.setCellFactory(new Callback<TreeTableColumn<TableRow, TableRow>, TreeTableCell<TableRow, TableRow>>() {
         @Override
         public TreeTableCell<TableRow, TableRow> call(final TreeTableColumn<TableRow, TableRow> column) {
            return new TreeTableCell<TableRow, TableRow>() {
               @Override
               protected void updateItem(final TableRow item, final boolean empty) {
                  super.updateItem(item, empty);
                  if (item == null || empty) {
                     setGraphic(null);
                     setText(null);
                  } else {
                     final String notes = item.getNotes();
                     final Label label = new Label(notes.isEmpty() ? EMPTY_NOTE : notes);
                     label.setUnderline(item.isUnderlined());
                     label.setTooltip(new Tooltip(notes));
                     this.setGraphic(label);
                  }
               }
            };
         }

      });
      noteColumn.setCellValueFactory(
            (final TreeTableColumn.CellDataFeatures<TableRow, TableRow> entry) -> new ReadOnlyObjectWrapper<>(
                  entry.getValue().getValue()));
      noteColumn.setMinWidth(200);
      noteColumn.impl_setReorderable(false);
      this.workTableTreeView.getColumns().add(noteColumn);

      final TreeTableColumn<TableRow, String> timeRangeColumn = new TreeTableColumn<>("Timeslot");
      timeRangeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<TableRow, String>("timeRange"));
      timeRangeColumn.setMinWidth(120);
      timeRangeColumn.impl_setReorderable(false);
      this.workTableTreeView.getColumns().add(timeRangeColumn);

      final TreeTableColumn<TableRow, String> timeSumColumn = new TreeTableColumn<>("Duration");
      timeSumColumn.setCellValueFactory(new TreeItemPropertyValueFactory<TableRow, String>("timeSum"));
      timeSumColumn.setMinWidth(60);
      timeSumColumn.impl_setReorderable(false);
      this.workTableTreeView.getColumns().add(timeSumColumn);

      final TreeTableColumn<TableRow, Button> buttonColumn = new TreeTableColumn<>("Controls");
      buttonColumn.setCellValueFactory(new TreeItemPropertyValueFactory<TableRow, Button>("buttonBox"));
      buttonColumn.setMinWidth(100);
      buttonColumn.setSortable(false);
      buttonColumn.impl_setReorderable(false);
      this.workTableTreeView.getColumns().add(buttonColumn);

      workTableTreeView.setShowRoot(false);

      workTableTreeView.setRoot(rootItem);
      rootItem.setExpanded(true);
   }

   private void updateReport(final LocalDate dateToShow) {
      this.currentReportDate = dateToShow;
      rootItem.getChildren().clear();
      reportRoot.requestFocus();

      this.currentDayLabel.setText(DateFormatter.toDayDateString(this.currentReportDate));
      final List<Work> currentWorkItems = model.getWorkRepository()
            .findByStartDateOrderByStartTimeAsc(this.currentReportDate);

      colorTimeLine.update(currentWorkItems, controller.calcSeconds(currentWorkItems));

      final SortedSet<Project> workedProjectsSet = currentWorkItems.stream().map(Work::getProject)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Project::getIndex))));

      long currentWorkSeconds = 0;
      long currentSeconds = 0;

      for (final Project project : workedProjectsSet) {
         final List<Work> onlyCurrentProjectWork = currentWorkItems.stream().filter(w -> w.getProject() == project)
               .collect(Collectors.toList());

         final long projectWorkSeconds = controller.calcSeconds(onlyCurrentProjectWork);

         currentSeconds += projectWorkSeconds;
         if (project.isWork()) {
            currentWorkSeconds += projectWorkSeconds;
         }

         final HBox projectButtonBox = new HBox();
         projectButtonBox.getChildren().add(createCopyProjectButton(onlyCurrentProjectWork));

         final Circle circle = new Circle(6, project.getColor());

         final TreeItem<TableRow> projectRow = new TreeItem<>(
               new ProjectTableRow(project, projectWorkSeconds, projectButtonBox), circle);

         for (final Work w : onlyCurrentProjectWork) {
            final HBox workButtonBox = new HBox(5.0);
            workButtonBox.getChildren().add(createCopyWorkButton(w));
            workButtonBox.getChildren().add(createEditWorkButton(w));
            workButtonBox.getChildren().add(createDeleteWorkButton(w));
            final TreeItem<TableRow> workRow = new TreeItem<>(new WorkTableRow(w, workButtonBox));
            projectRow.getChildren().add(workRow);
         }

         projectRow.setExpanded(true);
         rootItem.getChildren().add(projectRow);

      }

      this.currentDayTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentSeconds));
      this.currentDayWorkTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentWorkSeconds));

      loadCalenderWidget();

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
            if (model.getWorkRepository().findByStartDateOrderByStartTimeAsc(item).isEmpty()) {
               setDisable(true);
               setStyle(FX_BACKGROUND_COLOR_NOT_WORKED);
            } else {
               setDisable(false);
               setStyle(FX_BACKGROUND_COLOR_WORKED);
            }
         }

      };

      myDatePicker.setDayCellFactory(dayCellFactory);
      final Node popupContent = datePickerSkin.getPopupContent();
      this.topBorderPane.setRight(popupContent);

   }

   private Button createDeleteWorkButton(final Work w) {
      final Button deleteButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
      deleteButton.setOnAction(e -> {
         LOG.info("Delete work clicked.");
         final Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Delete Work");
         alert.setHeaderText("Delete work item");
         alert.setContentText(w.toString());
         alert.initOwner(stage);

         final Optional<ButtonType> result = alert.showAndWait();

         result.ifPresent(buType -> {
            if (buType.equals(ButtonType.OK)) {
               controller.deleteWork(w);
               this.update();
            }
         });
      });
      return deleteButton;
   }

   private Button createEditWorkButton(final Work work) {
      final Button editButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
      editButton.setOnAction(e -> {
         LOG.info("Edit work clicked.");
         final Dialog<Work> dialog = setupEditWorkDialog(work);

         final Optional<Work> result = dialog.showAndWait();

         result.ifPresent(editedWork -> {
            controller.editWork(work, editedWork);

            this.update();
         });
      });
      return editButton;
   }

   private Dialog<Work> setupEditWorkDialog(final Work work) {
      final Dialog<Work> dialog = new Dialog<>();
      dialog.initOwner(stage);
      dialog.setTitle(EDIT_WORK_DIALOG_TITLE);
      dialog.setHeaderText(EDIT_WORK_DIALOG_TITLE);
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

      dialog.getDialogPane()
            .lookupButton(ButtonType.OK)
            .disableProperty()
            .bind(manageWorkController.validProperty().not());

      dialog.setResultConverter(dialogButton -> {
         if (dialogButton == ButtonType.OK) {
            return manageWorkController.getWorkFromUserInput();
         }
         return null;
      });

      return grid;
   }

   private Button createCopyProjectButton(final List<Work> projectWork) {
      final Button copyButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.CLIPBOARD));
      final EventHandler<ActionEvent> eventListener = actionEvent -> {
         LOG.debug("Copy to Clipboard clicked.");
         final ProjectReport pr = new ProjectReport(projectWork.size());
         for (int j = 0; j < projectWork.size(); j++) {
            final Work work = projectWork.get(j);
            final String currentWorkNote = work.getNotes();
            pr.appendToWorkNotes(currentWorkNote);
         }
         final Clipboard clipboard = Clipboard.getSystemClipboard();
         final ClipboardContent content = new ClipboardContent();
         content.putString(pr.getNotes(true));
         clipboard.setContent(content);
      };

      copyButton.setOnAction(eventListener);
      return copyButton;
   }

   private Node createCopyWorkButton(final Work w) {
      final Button copyButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.CLIPBOARD));
      final EventHandler<ActionEvent> eventListener = actionEvent -> {
         LOG.debug("Copy to Clipboard clicked.");
         final Clipboard clipboard = Clipboard.getSystemClipboard();
         final ClipboardContent content = new ClipboardContent();
         content.putString(w.getNotes());
         clipboard.setContent(content);
      };

      copyButton.setOnAction(eventListener);
      return copyButton;
   }

   public void update() {
      updateReport(this.currentReportDate);
   }

   public void setStage(final Stage stage) {
      this.stage = stage;
   }
}
