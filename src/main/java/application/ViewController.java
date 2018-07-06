package application;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import application.Resources.RESOURCE;
import application.time.Interval;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

@Component
public class ViewController {
   @FXML
   private Pane pane;

   @FXML
   private VBox availableProjectVbox;

   @FXML
   private VBox projectsVBox;

   @FXML
   private Label bigTimeLabel;
   @FXML
   private Label allTimeLabel;
   @FXML
   private Label todayAllSeconds;

   @FXML
   private Label currentProjectLabel;

   @FXML
   private Button minimizeButton;
   @FXML
   private Button closeButton;

   @FXML
   private TextArea textArea;

   @FXML
   private Canvas canvas;

   private final LocalDateTime startTime = LocalDateTime.now();

   boolean pressed = false;
   double startX = -1;

   class Delta {
      double x, y;
   }

   Delta dragDelta = new Delta();

   class Work {
      public LocalDateTime startTime;
      public LocalDateTime endTime;

      public Project project;
      public String notes;

      public Work(final LocalDateTime startTime, final LocalDateTime endTime, final Project project,
            final String notes) {
         super();
         this.startTime = startTime;
         this.endTime = endTime;
         this.project = project;
         this.notes = notes;
      }

   }

   List<Work> workItems = new ArrayList<>();

   Work currentWork;

   // TODO alle 10min stand speichern

   public void changeProject(final Project newProject) {

      final LocalDateTime now = LocalDateTime.now();
      if (currentWork != null) {
         currentWork.endTime = now;
         currentWork.notes = textArea.getText();
         if (currentWork.notes.isEmpty()) {
            currentWork.notes = "- No notes -";
         }

         final String time = secondsToHHMMSS(Duration.between(currentWork.startTime, currentWork.endTime).getSeconds());
         Log.info("You worked from '%s' to '%s' (%s) on project '%s' with notes '%s'", currentWork.startTime,
               currentWork.endTime, time, currentWork.project.name, currentWork.notes);

         try {
            final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
            final String startTime = formatter1.format(currentWork.startTime);
            final String endTime = DateTimeFormatter.ofPattern("dd.MM.yyyy -  HH:mm:ss").format(currentWork.endTime);
            final String s = "------------------------------------\n" + currentWork.project.name + "\t" + startTime
                  + " - " + endTime + "( " + time + " )" + "\n" + currentWork.notes + "\n\n";

            appendToFile(s);
         } catch (final IOException e) {
            // exception handling left as an exercise for the reader
         }

      }

      // Save last work
      final Work work = new Work(now, now, newProject, "");
      workItems.add(work);

      // Start new work
      currentWork = work;
   }

   private void appendToFile(final String string) throws IOException {
      final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");
      final String fileName = formatter2.format(currentWork.startTime) + ".txt";
      final Path path = Paths.get(fileName);
      if (path.toFile().createNewFile()) {
         Log.info("Log file '%s' was created.", path);
      }
      Files.write(path, string.getBytes(), StandardOpenOption.APPEND);
   }

   Map<Project, Label> elapsedProjectTimeLabelMap = new HashMap<>();

   boolean wasDragged = false;

   Canvas taskbarCanvas = new Canvas(32, 32);

   @FXML
   private void initialize() {

      bigTimeLabel.setText("00:00:00");
      allTimeLabel.setText("00:00:00");
      todayAllSeconds.setText("00:00:00");

      textArea.setWrapText(true);
      textArea.setEditable(false);

      minimizeButton.setOnAction((ae) -> {
         DemoApplication.stage.setIconified(true);
      });
      closeButton.setOnAction((ae) -> {
         DemoApplication.stage.close();
      });

      changeProject(DemoApplication.idleProject);

      updateProjectView();
      for (final Project p : DemoApplication.projects) {
         final FXMLLoader loader = new FXMLLoader();
         loader.setLocation(Resources.getResource(RESOURCE.FXML_PROJECT_LAYOUT));
         try {
            final Pane projectElement = (Pane) loader.load();

            final Label projectNameLabel = (Label) projectElement.getChildren().get(0);
            final Label elapsedTimeLabel = (Label) projectElement.getChildren().get(1);
            elapsedProjectTimeLabelMap.put(p, elapsedTimeLabel);

            projectNameLabel.setText(p.name);
            final Color color = p.color;
            final double dimFactor = .6;
            final Color dimmedColor = new Color(color.getRed() * dimFactor, color.getGreen() * dimFactor,
                  color.getBlue() * dimFactor, 1);
            projectNameLabel.setTextFill(dimmedColor);

            projectNameLabel.setUserData(p);
            projectNameLabel.setOnMouseClicked((a) -> {
               if (!wasDragged) {
                  changeProject((Project) ((Node) a.getSource()).getUserData());
                  updateProjectView();
               }
            });
            projectNameLabel.setOnMouseEntered(ae -> {
               projectNameLabel.setTextFill(p.color);
               final Bloom bloom = new Bloom();
               bloom.setThreshold(0.3);
               projectNameLabel.setEffect(bloom);
               projectNameLabel.setUnderline(true);
            });
            projectNameLabel.setOnMouseExited(ae -> {
               projectNameLabel.setTextFill(dimmedColor);
               projectNameLabel.setEffect(null);
               projectNameLabel.setUnderline(false);
            });

            availableProjectVbox.getChildren().add(projectElement);

         } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      // Change color
      pane.setOnMouseEntered((a) -> {
         String style = changeStyleAttribute(pane.getStyle(), "fx-background-color", "rgba(54,143,179,.1)");
         style = changeStyleAttribute(style, "fx-border-color", "rgba(54,143,179,.3)");
         pane.setStyle(style);

         textArea.setEditable(true);
         projectsVBox.setVisible(true);

      });

      pane.setOnMouseExited((a) -> {
         String style = changeStyleAttribute(pane.getStyle(), "fx-background-color", "rgba(54,143,179,0.01)");
         style = changeStyleAttribute(style, "fx-border-color", "rgba(54,143,179,0)");
         pane.setStyle(style);

         textArea.setEditable(false);
         projectsVBox.setVisible(false);
      });

      // Drag stage
      pane.setOnMousePressed((mouseEvent) -> {
         // record a delta distance for the drag and drop operation.
         dragDelta.x = stage.getX() - mouseEvent.getScreenX();
         dragDelta.y = stage.getY() - mouseEvent.getScreenY();
         wasDragged = false;
      });

      pane.setOnMouseDragged((mouseEvent) -> {
         stage.setX(mouseEvent.getScreenX() + dragDelta.x);
         stage.setY(mouseEvent.getScreenY() + dragDelta.y);
         wasDragged = true;
      });

      Interval.registerCallBack(() -> {
         final LocalDateTime now = LocalDateTime.now();
         currentWork.endTime = now;

         final long currentWorkSeconds = Duration.between(currentWork.startTime, currentWork.endTime).getSeconds();
         final long todayWorkingSeconds = calcTodaysWorkSeconds();

         bigTimeLabel.setText(secondsToHHMMSS(currentWorkSeconds));
         // TODO only count work time (not idle or kicker)
         allTimeLabel.setText(secondsToHHMMSS(todayWorkingSeconds));
         if (todayWorkingSeconds > 60 * 60 * 8) {
            allTimeLabel.setTextFill(Color.DARKGREEN);
         }

         todayAllSeconds.setText(secondsToHHMMSS(Duration.between(startTime, now).getSeconds()));

         final GraphicsContext gc = canvas.getGraphicsContext2D();

         gc.setFill(new Color(.3, .3, .3, .3));
         gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
         gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

         int currentX = 0;
         for (final Work w : workItems) {
            final long maxSeconds = 60 * 60 * 10;
            final long workedSeconds = Duration.between(w.startTime, w.endTime).getSeconds();
            final int fillX = (int) ((float) workedSeconds / maxSeconds * canvas.getWidth());
            gc.setFill(w.project.color);
            gc.fillRect(currentX, 0, fillX, canvas.getHeight());
            currentX += fillX;
         }

         for (final Project p : elapsedProjectTimeLabelMap.keySet()) {
            final Label label = elapsedProjectTimeLabelMap.get(p);

            final long seconds = workItems.stream().filter((work) -> work.project == p).mapToLong(work -> {
               return Duration.between(work.startTime, work.endTime).getSeconds();
            }).sum();
            label.setText(secondsToHHMMSS(seconds));
         }

         // update taskbar icon

         final GraphicsContext gcIcon = taskbarCanvas.getGraphicsContext2D();
         // gc.drawImage(new
         // Image(Resources.getResource(RESOURCE.ICON_DemoApplication).toString()), 0,
         // 0);

         gcIcon.clearRect(0, 0, taskbarCanvas.getWidth(), taskbarCanvas.getHeight());
         gcIcon.setFill(currentWork.project.color);
         gcIcon.fillRect(1, 27, 31, 5);

         gcIcon.setStroke(DemoApplication.taskBarColor);
         gcIcon.setTextAlign(TextAlignment.CENTER);
         gcIcon.setFont(new Font("Arial", 12));
         gcIcon.strokeText(secondsToHHMMSS(currentWorkSeconds).replaceFirst(":", ":\n"),
               Math.round(taskbarCanvas.getWidth() / 2), Math.round(taskbarCanvas.getHeight() / 2) - 5);

         final SnapshotParameters snapshotParameters = new SnapshotParameters();
         snapshotParameters.setFill(Color.TRANSPARENT);
         final WritableImage image = taskbarCanvas.snapshot(snapshotParameters, null);

         final StackPane layout = new StackPane();
         layout.getChildren().addAll(new ImageView(image));

         final BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
         final Image icon = SwingFXUtils.toFXImage(bi, null);

         final ObservableList<Image> icons = DemoApplication.stage.getIcons();
         icons.addAll(icon);
         if (icons.size() > 1) {
            icons.remove(0);
         }

      });

   }

   private void updateProjectView() {
      textArea.setText("");
      currentProjectLabel.setText(currentWork.project.name);
      final Circle circle = new Circle(4);
      circle.setFill(currentWork.project.color);
      currentProjectLabel.setGraphic(circle);
   }

   public void createSummary() throws IOException {

      final StringBuilder sb = new StringBuilder("\n******* Summary *******\n\n");

      for (final Project p : elapsedProjectTimeLabelMap.keySet()) {

         Stream<Work> work = workItems.stream().filter((w) -> w.project == p);
         final long seconds = work.mapToLong(w -> {
            return Duration.between(w.startTime, w.endTime).getSeconds();
         }).sum();

         work = workItems.stream().filter((w) -> w.project == p);
         sb.append(p.name + "\t\t\t" + secondsToHHMMSS(seconds) + "\n");
         work.forEach(
               w -> sb.append("Duration: " + secondsToHHMMSS(Duration.between(w.startTime, w.endTime).getSeconds())
                     + "\n" + "- " + w.notes + "\n"));
         sb.append("--------------------------\n");
      }

      appendToFile(sb.toString());

   }

   private long calcTodaysWorkSeconds() {
      return workItems.stream().filter((work) -> work.project.isWork).mapToLong((work) -> {
         return Duration.between(work.startTime, work.endTime).getSeconds();
      }).sum();
   }

   private String secondsToHHMMSS(final long currentWorkSeconds) {
      final int hours = (int) (currentWorkSeconds / 3600);
      final int minutes = (int) ((currentWorkSeconds % 3600) / 60);

      final int sec = (int) (((currentWorkSeconds % 3600) % 60));

      final String a = (hours > 9 ? hours : "0" + hours) + ":" + (minutes > 9 ? minutes : "0" + minutes) + ":"
            + (sec > 9 ? sec : "0" + sec);
      return a;
   }

   public static String changeStyleAttribute(final String style, final String attribute, final String newValue) {

      String newStyle = "";
      // Log.info("Old style %s", style);
      final String newStyleAttribute = "-" + attribute + ": " + newValue + "; ";
      if (style.contains(attribute)) {
         // Log.info("replacing");
         newStyle = style.replaceAll("-" + attribute + ": " + "[^;]+;", newStyleAttribute);
      } else {
         // Log.info("adding");
         newStyle = style + newStyleAttribute;
      }

      // Log.info("new style: %s", newStyle);

      return newStyle;
   }

   Stage stage;

   public void setStage(final Stage primaryStage) {
      this.stage = primaryStage;
   }

}
