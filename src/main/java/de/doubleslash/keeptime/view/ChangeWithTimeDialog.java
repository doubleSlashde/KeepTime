package de.doubleslash.keeptime.view;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
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

public class ChangeWithTimeDialog {

   private static final Logger LOG = LoggerFactory.getLogger(ChangeWithTimeDialog.class);
   private static final String TIME_ZERO = "00:00:00";

   private LongProperty activeWorkSecondsProperty;
   private Model model;
   private Dialog<Integer> dialog;
   private boolean ctrlIsPressed = false;

   public void setUpDialog(final Project p) {

      LOG.info("Change with time");
      dialog = new Dialog<>();
      dialog.setTitle("Change project with time transfer");
      dialog.setHeaderText("Choose the time to transfer");
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
      okButton.setDefaultButton(true);
      final Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
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
      final Slider slider = setUpSliderChangeWithTimeMenuItem(activeWorkSecondsProperty);

      okButton.setOnKeyPressed(ke -> slider.requestFocus());

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

      grid.add(new Label("New project duration: " + p.getName()), 0, gridRow);
      final Label newProjectTimeLabel = new Label(TIME_ZERO);
      grid.add(newProjectTimeLabel, 1, gridRow);
      gridRow++;

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

      dialog.getDialogPane().setContent(vBox);

      dialog.setResultConverter(dialogButton -> {
         if (dialogButton == ButtonType.OK) {
            return slider.valueProperty().intValue() * 60;
         }
         return null;
      });

   }

   public Optional<Integer> show() {

      return dialog.showAndWait();

   }

   public void setModel(final Model model) {
      this.model = model;
   }

   public void setActiveWorkSecondsProperty(final LongProperty activeWorkSecondsProperty) {
      this.activeWorkSecondsProperty = activeWorkSecondsProperty;
   }

   private Slider setUpSliderChangeWithTimeMenuItem(final LongProperty activeWorkSecondsProperty) {
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

      slider.setOnKeyPressed(ke -> {
         if (!slider.isFocused()) {
            slider.requestFocus();
         }

         if (ke.getCode() == KeyCode.CONTROL) {
            ctrlIsPressed = true;
         }

         if (ke.getCode() == KeyCode.LEFT && ctrlIsPressed) {
            slider.adjustValue(slider.getValue() - 5);
         }

         if (ke.getCode() == KeyCode.RIGHT && ctrlIsPressed) {
            slider.adjustValue(slider.getValue() + 5);
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
