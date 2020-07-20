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

import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.view.common.SearchableProjectCombobox;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
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
   private SearchableProjectCombobox projectComboBox;

   public void setModel(final Model model) {
      this.model = model;
      projectComboBox.setProjects(model.getSortedAvailableProjects());
   }

   @FXML
   private void initialize() {

      setUpTimeSpinner(startTimeSpinner);

      setUpTimeSpinner(endTimeSpinner);

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

   public void initializeWith(final Work work) {
      LOG.info("Setting values.");
      projectComboBox.setProject(work.getProject(), model.hoverBackgroundColor.get(), model.hoverFontColor.get());
      startDatePicker.setValue(work.getStartTime().toLocalDate());
      endDatePicker.setValue(work.getEndTime().toLocalDate());

      startTimeSpinner.getValueFactory().setValue(work.getStartTime().toLocalTime());
      endTimeSpinner.getValueFactory().setValue(work.getEndTime().toLocalTime());

      noteTextArea.setText(work.getNotes());

   }

   public Work getWorkFromUserInput() {
      return new Work(startDatePicker.getValue(),
            LocalDateTime.of(startDatePicker.getValue(), startTimeSpinner.getValue()),
            LocalDateTime.of(endDatePicker.getValue(), endTimeSpinner.getValue()), projectComboBox.getValue(),
            noteTextArea.getText());
   }

}
