package de.doubleslash.keeptime.view;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.stream.IntStream;

public class TimePickerComboBox extends HBox {

   private final ComboBox<Integer> hourBox = new ComboBox<>();
   private final ComboBox<Integer> minuteBox = new ComboBox<>();

   private final ObjectProperty<LocalTime> value = new SimpleObjectProperty<>(this, "value");

   private boolean updating = false;

   public TimePickerComboBox() {
      init24HourControls();
      attachListeners();
      setSpacing(4);
   }

   public ObjectProperty<LocalTime> valueProperty() {
      return value;
   }

   public LocalTime getValue() {
      return value.get();
   }

   public void setValue(LocalTime time) {
      value.set(time);
   }

   private void attachListeners() {
      hourBox.valueProperty().addListener((obs, ov, nv) -> updateValueFromUi());
      minuteBox.valueProperty().addListener((obs, ov, nv) -> updateValueFromUi());
      value.addListener((obs, ov, nv) -> updateUiFromValue(nv));
   }

   private void init24HourControls() {
      // Populate hours 0-23
      hourBox.getItems().setAll(IntStream.range(0, 24).boxed().toList());
      // Populate minutes 0-59
      minuteBox.getItems().setAll(IntStream.range(0, 60).boxed().toList());

      // Two-digit formatting for hour/minute
      StringConverter<Integer> twoDigitConverter = new StringConverter<>() {
         @Override public String toString(Integer object) {
            return object == null ? "" : String.format("%02d", object);
         }
         @Override public Integer fromString(String string) {
            try { return Integer.valueOf(string); } catch (Exception e) { return null; }
         }
      };
      hourBox.setConverter(twoDigitConverter);
      minuteBox.setConverter(twoDigitConverter);
      // Ensure editors exist for formatting updates
      hourBox.setEditable(true);
      minuteBox.setEditable(true);

      getChildren().clear();
      getChildren().addAll(hourBox, new Label(":"), minuteBox);
   }

   private void updateValueFromUi() {
      if (updating) return;

      Integer hSel = hourBox.getValue();
      Integer mSel = minuteBox.getValue();
      if (hSel == null || mSel == null) {
         value.set(null);
         return;
      }

      int hour24 = hSel;

      updating = true;
      try {
         value.set(LocalTime.of(hour24, mSel));
      } finally {
         updating = false;
      }
   }

   private void updateUiFromValue(LocalTime t) {
      if (updating) return;

      updating = true;
      try {
         if (t == null) {
            hourBox.getSelectionModel().clearSelection();
            minuteBox.getSelectionModel().clearSelection();
            return;
         }

         int h24 = t.getHour();
         int m = t.getMinute();
         hourBox.setValue(h24);
         minuteBox.setValue(m);

         Platform.runLater(() -> {
            minuteBox.getEditor().setText(minuteBox.getConverter().toString(minuteBox.getValue()));
            hourBox.getEditor().setText(hourBox.getConverter().toString(hourBox.getValue()));
         });
      } finally {
         updating = false;
      }
   }
}