package application.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.scene.control.skin.DatePickerSkin;

import application.Main;
import application.common.DateFormatter;
import application.model.Project;
import application.model.Work;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

public class ReportController {
   @FXML
   private HBox topHBox;

   @FXML
   private Label currentDayLabel;
   @FXML
   private Label currentDayWorkTimeLabel;
   @FXML
   private Label currentDayTimeLabel;

   @FXML
   private GridPane gridPane;

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
                  if (Main.workRepository.findByCreationDate(item).isEmpty()) {
                     setDisable(true);
                     setStyle("-fx-background-color: #ffc0cb;");
                     setTooltip(new Tooltip("You did not work here. Lazy!"));// may only work if enabled
                  }
               }
            };
         }
      };
      datePicker.setDayCellFactory(dayCellFactory);
      datePicker.valueProperty().addListener((observable, oldvalue, newvalue) -> {
         Log.info("Datepicker selected value changed to {}", newvalue);
         updateReport(newvalue);

      });
      updateReport(datePicker.getValue());
      // HACK to show calendar from datepicker
      // https://stackoverflow.com/questions/34681975/javafx-extract-calendar-popup-from-datepicker-only-show-popup
      final DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
      final Node popupContent = datePickerSkin.getPopupContent();

      // root.setCenter(popupContent);
      topHBox.getChildren().add(popupContent);
   }

   long currentWorkSeconds = 0;
   long currentSeconds = 0;

   private void updateReport(final LocalDate newvalue) {
      currentDayLabel.setText(DateFormatter.toDayDateString(newvalue));
      final List<Work> currentWorkItems = Main.workRepository.findByCreationDate(newvalue);

      final Set<Project> workedProjectsSet = currentWorkItems.stream().map(m -> {
         return m.getProject();
      }).collect(Collectors.toSet());

      gridPane.getChildren().clear();
      gridPane.getRowConstraints().clear();

      int rowIndex = 0;
      currentWorkSeconds = 0;
      currentSeconds = 0;
      for (final Project project : workedProjectsSet) {
         final Label projectName = new Label(project.getName());
         projectName.setFont(Font.font("System", FontWeight.BOLD, 15));
         gridPane.add(projectName, 0, rowIndex);

         final List<Work> onlyCurrentProjectWork = currentWorkItems.stream().filter(w -> w.getProject() == project)
               .collect(Collectors.toList());

         final long todaysWorkSeconds = onlyCurrentProjectWork.stream().mapToLong(work -> {
            return DateFormatter.getSecondsBewtween(work.getStartTime(), work.getEndTime());
         }).sum();

         currentSeconds += todaysWorkSeconds;
         if (project.isWork()) {
            currentWorkSeconds += todaysWorkSeconds;
         }

         final Label workedTimeLabel = new Label(DateFormatter.secondsToHHMMSS(todaysWorkSeconds));
         workedTimeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
         gridPane.add(workedTimeLabel, 1, rowIndex);
         rowIndex++;
         final RowConstraints row1 = new RowConstraints(17, 17, 17);
         // row1.setVgrow(Priority.ALWAYS);
         // gridPane.getRowConstraints().add(row1);

         for (int j = 0; j < onlyCurrentProjectWork.size(); j++) {
            final Work work = onlyCurrentProjectWork.get(j);
            final String workedHours = DateFormatter
                  .secondsToHHMMSS(DateFormatter.getSecondsBewtween(work.getStartTime(), work.getEndTime()));

            final Label commentLabel = new Label(work.getNotes());
            commentLabel.setFont(Font.font("System", FontWeight.NORMAL, 15));
            commentLabel.setWrapText(true);
            gridPane.add(commentLabel, 0, rowIndex);

            final Label workedHoursLabel = new Label(workedHours);
            workedHoursLabel.setFont(Font.font("System", FontWeight.NORMAL, 15));
            gridPane.add(workedHoursLabel, 1, rowIndex);

            final RowConstraints row2 = new RowConstraints(15, 15, 15);

            // row2.setVgrow(Priority.ALWAYS);
            // gridPane.getRowConstraints().add(row2);
            rowIndex++;
         }
      }
      gridPane.setGridLinesVisible(true);

      currentDayTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentSeconds));
      currentDayWorkTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentWorkSeconds));
   }
}
