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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import de.doubleslash.keeptime.common.ColorHelper;
import de.doubleslash.keeptime.common.StyleUtils;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;

public class ManageWorkController {

   private static final Logger LOG = LoggerFactory.getLogger(ManageWorkController.class);

   private Model model;

   @FXML
   private GridPane grid;

   @FXML
   private DatePicker startDatePicker;

   @FXML
   private Spinner<LocalTime> startTimeSpinner;

   @FXML
   private Spinner<LocalTime> endTimeSpinner;

   @FXML
   private DatePicker endDatePicker;

   @FXML
   private TextArea noteTextArea;

   @FXML
   private ComboBox<Project> projectComboBox;

   @FXML
   private Label errorLabel;

   private boolean comboChange;
   private Project selectedProject;
   private BooleanProperty isValidProperty = new SimpleBooleanProperty();

   private FilteredList<Project> filteredList;

   public void setModel(final Model model) {
      this.model = model;
      filteredList = new FilteredList<>(model.getSortedAvailableProjects());
      projectComboBox.setItems(filteredList);
   }

   @FXML
   private void initialize() {

      setUpTimeSpinner(startTimeSpinner);

      setUpTimeSpinner(endTimeSpinner);

      setUpTimeRestriction();

      setProjectUpComboBox();

      Platform.runLater(() -> projectComboBox.requestFocus());
   }

   private void setUpTimeRestriction() {

      BooleanBinding isValidBinding = Bindings.createBooleanBinding(() -> {
         if (startTimeSpinner.getValue() == null || endTimeSpinner.getValue() == null
               || startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            return false;
         }

         LocalDateTime start = LocalDateTime.of(startDatePicker.getValue(), startTimeSpinner.getValue());
         LocalDateTime end = LocalDateTime.of(endDatePicker.getValue(), endTimeSpinner.getValue());

         if (start.isAfter(end)) {
            return false;
         } else {
            return true;
         }
      }, startTimeSpinner.valueProperty(), endTimeSpinner.valueProperty(), endDatePicker.valueProperty(),
            startDatePicker.valueProperty());

      this.isValidProperty.bind(isValidBinding);

      errorLabel.textProperty().bind(Bindings.createStringBinding(() -> {
         if (Boolean.TRUE.equals(isValidProperty.get())) {
            return "";
         } else {
            return "startDate has to be before endDate";
         }

      }, isValidProperty));
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
               setValue(time.minusMinutes(steps));
            }

         }

         @Override
         public void increment(final int steps) {
            if (getValue() == null) {
               setValue(LocalTime.now());
            } else {
               final LocalTime time = getValue();
               setValue(time.plusMinutes(steps));
            }

         }

      });

      spinner.getValueFactory().setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

   }

   private void setProjectUpComboBox() {
      // color Dropdown Options
      projectComboBox.setCellFactory(listView -> new ListCell<Project>() {

         @Override
         protected void updateItem(final Project project, final boolean empty) {
            super.updateItem(project, empty);
            if (project == null || empty) {
               setGraphic(null);
            } else {
               setColor(this, model.hoverBackgroundColor.get());

               setTextFill(project.getColor());
               setText(project.getName());

               setUnderline(project.isWork());
            }
         }
      });

      // set text of selected value
      projectComboBox.setConverter(new StringConverter<Project>() {
         @Override
         public String toString(final Project project) {
            if (project == null) {
               return null;
            } else {
               return project.getName();
            }
         }

         @Override
         public Project fromString(final String string) {
            // ignores String and gets selected Value of ComboBox
            return projectComboBox.getValue();
         }
      });

      // needs to set again because editable is ignored from fxml because of custom preselection of current Project
      projectComboBox.setEditable(true);

      projectComboBox.valueProperty().addListener(
            (final ObservableValue<? extends Project> observable, final Project oldValue, final Project newValue) -> {
               if (newValue == null) {
                  return;
               }

               selectedProject = newValue;
               comboChange = true;
               // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
               Platform.runLater(() -> {
                  setTextColor(projectComboBox.getEditor(), newValue.getColor());
               });
            }

      );

      enableStrgA_combo();

      projectComboBox.getEditor().textProperty().addListener(new ChangeListener<String>() {

         boolean isValidProject = true;

         @Override
         public void changed(final ObservableValue<? extends String> observable, final String oldValue,
               final String newValue) {

            // ignore selectionChanges
            if (comboChange) {
               comboChange = false;
               isValidProject = true;
               return;
            }

            // is necessary to not autoselect same Project if Project was selected
            if (isValidProject) {
               isValidProject = false;
               projectComboBox.getSelectionModel().clearSelection();
               // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
               Platform.runLater(() -> {
                  setTextColor(projectComboBox.getEditor(), model.hoverFontColor.get());
               });
            }

            // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
            Platform.runLater(() -> {
               projectComboBox.hide();

               final String searchText = projectComboBox.getEditor().getText();
               filteredList.setPredicate(
                     project -> ProjectsListViewController.doesProjectMatchSearchFilter(searchText, project));

               if (projectComboBox.getEditor().focusedProperty().get()) {
                  projectComboBox.show();
               }

            });

         }
      });

      // manages Focusbehaviour
      projectComboBox.getEditor().focusedProperty().addListener((final ObservableValue<? extends Boolean> observable,
            final Boolean oldIsFocused, final Boolean newIsFocused) -> {
         if (newIsFocused) {
            // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
            Platform.runLater(() -> projectComboBox.getEditor().selectAll());
         } else {
            // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
            Platform.runLater(() -> projectComboBox.hide());
         }

      });

      // on
      projectComboBox.setOnKeyReleased(ke -> {
         if (ke.getCode() == KeyCode.ENTER && projectComboBox.getSelectionModel().isEmpty()) {
            if (!projectComboBox.getItems().isEmpty()) {
               projectComboBox.getSelectionModel().selectFirst();
               comboChange = true;
            }
         }

      });

   }

   public void initializeWith(final Work work) {
      LOG.info("Setting values.");
      selectedProject = work.getProject();
      startDatePicker.setValue(work.getStartTime().toLocalDate());
      endDatePicker.setValue(work.getEndTime().toLocalDate());

      startTimeSpinner.getValueFactory().setValue(work.getStartTime().toLocalTime());
      endTimeSpinner.getValueFactory().setValue(work.getEndTime().toLocalTime());

      noteTextArea.setText(work.getNotes());

      projectComboBox.getSelectionModel().select(work.getProject());

      setColor(projectComboBox, model.hoverBackgroundColor.get());
      setColor(projectComboBox.getEditor(), model.hoverBackgroundColor.get());

      setTextColor(projectComboBox.getEditor(), model.hoverFontColor.get());
   }

   private void enableStrgA_combo() {
      // strg+a Behaviour bug hack
      // https://stackoverflow.com/questions/51943654/javafx-combobox-make-control-a-select-all-in-text-box-while-dropdown-is-visi
      projectComboBox.setOnShown(e -> {
         final ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) projectComboBox.getSkin();
         final ListView<?> list = (ListView<?>) skin.getPopupContent();
         final KeyCodeCombination ctrlA = new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN);
         list.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (ctrlA.match(keyEvent)) {
               projectComboBox.getEditor().selectAll();
            }
         });
         projectComboBox.setOnShown(null);
      });
   }

   private void setColor(final Node object, final Color color) {
      final String style = StyleUtils.changeStyleAttribute(object.getStyle(), "fx-background-color",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      object.setStyle(style);
   }

   public Work getWorkFromUserInput() {
      return new Work(LocalDateTime.of(startDatePicker.getValue(), startTimeSpinner.getValue()),
            LocalDateTime.of(endDatePicker.getValue(), endTimeSpinner.getValue()), selectedProject,
            noteTextArea.getText());
   }

   private void setTextColor(final Node object, final Color color) {
      final String style = StyleUtils.changeStyleAttribute(object.getStyle(), "fx-text-fill",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      object.setStyle(style);
   }

   public BooleanProperty validProperty() {
      return this.isValidProperty;
   }

}
