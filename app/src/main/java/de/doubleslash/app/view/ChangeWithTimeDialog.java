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

package de.doubleslash.app.view;

import de.doubleslash.app.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.app.common.DateFormatter;
import de.doubleslash.app.model.Model;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ChangeWithTimeDialog extends Dialog<Integer> {

   private static final Logger LOG = LoggerFactory.getLogger(ChangeWithTimeDialog.class);
   private static final String TIME_ZERO = "00:00:00";
   private static final int FAST_MINUTE_STEPS = 5;

   private final Model model;
   private final LongProperty activeWorkSecondsProperty;
   private final Project projectToChangeTo;

   private boolean ctrlIsPressed = false;

   public ChangeWithTimeDialog(final Model model, final LongProperty activeWorkSecondsProperty,
         final Project projectToChangeTo) {
      this.model = model;
      this.activeWorkSecondsProperty = activeWorkSecondsProperty;
      this.projectToChangeTo = projectToChangeTo;

      setUpDialog();
   }

   private void setUpDialog() {
      setTitle("Change project with time transfer");
      setHeaderText("Choose the time to transfer");
      getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
      okButton.setDefaultButton(true);
      final Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
      cancelButton.setDefaultButton(false);

      final VBox vBox = new VBox();
      final Label description = new Label(
            "Choose the amount of minutes to transfer from the active project to the new project");
      description.setWrapText(true);
      vBox.getChildren().add(description);

      final GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));
      int gridRow = 0;
      grid.add(new Label("Minutes to transfer"), 0, gridRow);
      final Slider slider = setupSlider();

      grid.add(slider, 1, gridRow);
      final Label minutesToTransferLabel = new Label("999 minute(s)");
      grid.add(minutesToTransferLabel, 2, gridRow);
      gridRow++;

      grid.add(new Label("New time distribution"), 0, gridRow);
      gridRow++;
      grid.add(new Label("Active project duration: " + model.activeWorkItem.get().getProject().getName()), 0, gridRow);
      final Label currentProjectTimeLabel = new Label(TIME_ZERO);
      grid.add(currentProjectTimeLabel, 1, gridRow);
      gridRow++;

      grid.add(new Label("New end and start time:"), 0, gridRow);
      final Label newEndTimeLabel = new Label(TIME_ZERO);
      grid.add(newEndTimeLabel, 1, gridRow);
      gridRow++;

      grid.add(new Label("New project duration: " + projectToChangeTo.getName()), 0, gridRow);
      final Label newProjectTimeLabel = new Label(TIME_ZERO);
      grid.add(newProjectTimeLabel, 1, gridRow);

      final Runnable updateLabelsRunnable = () -> {
         final long minutesOffset = slider.valueProperty().longValue();
         final long secondsOffset = minutesOffset * 60;

         final long secondsActiveWork = activeWorkSecondsProperty.get() - secondsOffset;
         final long secondsNewWork = 0 + secondsOffset;
         minutesToTransferLabel.setText(minutesOffset + " minute(s)");
         currentProjectTimeLabel.setText(DateFormatter.secondsToHHMMSS(secondsActiveWork));
         newProjectTimeLabel.setText(DateFormatter.secondsToHHMMSS(secondsNewWork));
         newEndTimeLabel.setText(
               DateFormatter.toTimeString(model.activeWorkItem.get().getEndTime().minusSeconds(secondsOffset)));
      };
      activeWorkSecondsProperty.addListener((obs, oldValue, newValue) -> updateLabelsRunnable.run());
      slider.valueProperty().addListener((obs, oldValue, newValue) -> updateLabelsRunnable.run());
      vBox.getChildren().add(grid);

      setOnShown(de -> {
         // workaround to set focus to slider when showing the dialog
         // onShown is actually called before the dialog is shown
         Platform.runLater(slider::requestFocus);
      });

      getDialogPane().setContent(vBox);

      setResultConverter(dialogButton -> {
         if (dialogButton == ButtonType.OK) {
            return slider.valueProperty().intValue() * 60;
         }
         return null;
      });

   }

   private Slider setupSlider() {
      final Slider slider = new Slider();

      slider.setMin(0);
      slider.maxProperty().bind(Bindings.createLongBinding(() -> {
         final long maxValue = activeWorkSecondsProperty.longValue() / 60;
         if (maxValue > 0) {
            slider.setDisable(false);
            return maxValue;
         }
         slider.setDisable(true);
         return 1l;
      }, activeWorkSecondsProperty));

      slider.setValue(0);
      slider.setShowTickLabels(true);
      slider.setShowTickMarks(true);
      slider.setMajorTickUnit(60);
      slider.setMinorTickCount(58);
      slider.setBlockIncrement(1);
      slider.setSnapToTicks(true);
      slider.setFocusTraversable(true);

      // allows you to make bigger steps with arrow keys on slider if ctrl is pressed
      slider.setOnKeyPressed(ke -> {
         if (ke.getCode() == KeyCode.CONTROL) {
            ctrlIsPressed = true;
         }

         if (ke.getCode() == KeyCode.LEFT && ctrlIsPressed) {
            slider.adjustValue(slider.getValue() - FAST_MINUTE_STEPS);
         }

         if (ke.getCode() == KeyCode.RIGHT && ctrlIsPressed) {
            slider.adjustValue(slider.getValue() + FAST_MINUTE_STEPS);
         }
      });

      slider.setOnKeyReleased(ke -> {
         if (ke.getCode() == KeyCode.CONTROL) {
            ctrlIsPressed = false;
         }
      });

      return slider;
   }

}
