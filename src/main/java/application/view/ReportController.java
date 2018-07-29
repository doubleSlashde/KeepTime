package application.view;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.scene.control.skin.DatePickerSkin;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class ReportController {
   @FXML
   private HBox topHBox;

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   @FXML
   private void initialize() {
      Log.info("Init reportController");

      final DatePicker datePicker = new DatePicker(LocalDate.now());
      //
      final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
         @Override
         public DateCell call(final DatePicker datePicker) {
            return new DateCell() {
               @Override
               public void updateItem(final LocalDate item, final boolean empty) {
                  super.updateItem(item, empty);
                  // if( !model has item ) {
                  final boolean disable = Math.random() > .5;
                  if (disable) {
                     setDisable(disable);
                     setStyle("-fx-background-color: #ffc0cb;");
                     setTooltip(new Tooltip("You did not work here. Lazy!"));// may only work if enabled
                  }
                  // }
               }
            };
         }
      };
      datePicker.setDayCellFactory(dayCellFactory);
      datePicker.valueProperty().addListener((observable, oldvalue, newvalue) -> {
         Log.info("Datepicker selected value changed to {}", newvalue);
      });
      // HACK to show calendar from datepicker
      // https://stackoverflow.com/questions/34681975/javafx-extract-calendar-popup-from-datepicker-only-show-popup
      final DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
      final Node popupContent = datePickerSkin.getPopupContent();

      // root.setCenter(popupContent);
      topHBox.getChildren().add(popupContent);
   }
}
