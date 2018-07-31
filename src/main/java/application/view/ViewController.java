package application.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import application.Main;
import application.common.DateFormatter;
import application.common.Resources;
import application.common.Resources.RESOURCE;
import application.controller.IController;
import application.model.Model;
import application.model.Project;
import application.model.Work;
import application.view.time.Interval;
import javafx.application.Platform;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
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
   private Button addNewProjectButton;
   @FXML
   private Button settingsButton;
   @FXML
   private Button calendarButton;

   @FXML
   private TextArea textArea;

   @FXML
   private Canvas canvas;

   boolean pressed = false;
   double startX = -1;

   class Delta {
      double x, y;
   }

   Delta dragDelta = new Delta();

   IController controller;
   Model model;

   public void setController(final IController controller, final Model model) {
      this.controller = controller;
      this.model = model;

      changeProject(model.idleProject, 0); // TODO not here!
      updateProjectView();
   }

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   public void changeProject(final Project newProject, final long minusSeconds) {
      controller.changeProject(newProject, textArea.getText(), minusSeconds);
      updateProjectView();
      textArea.setText("");
   }

   Map<Project, Label> elapsedProjectTimeLabelMap = new HashMap<>();

   boolean wasDragged = false;

   Canvas taskbarCanvas = new Canvas(32, 32);

   BooleanProperty mouseHovering = new SimpleBooleanProperty(false);

   Stage reportStage;
   ReportController reportController;

   Stage settingsStage;
   SettingsController settingsController;

   Map<Project, Node> projectSelectionNodeMap;

   @FXML
   private void initialize() throws IOException {

      loadSubStages();

      bigTimeLabel.setText("00:00:00");
      allTimeLabel.setText("00:00:00");
      todayAllSeconds.setText("00:00:00");

      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.editableProperty().bind(mouseHovering);
      projectsVBox.visibleProperty().bind(mouseHovering);

      minimizeButton.setOnAction((ae) -> {
         Main.stage.setIconified(true);
      });
      closeButton.setOnAction((ae) -> {
         Main.stage.close();
      });

      addNewProjectButton.setOnAction((ae) -> {
         Log.info("Add new project clicked");
         // TODO somewhat duplicate dialog of create and edit
         final Dialog<Project> dialog = new Dialog<>();
         dialog.setTitle("Create new project");
         dialog.setHeaderText("Create a new project");

         dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

         final GridPane grid = new GridPane();
         grid.setHgap(10);
         grid.setVgap(10);
         grid.setPadding(new Insets(20, 150, 10, 10));

         grid.add(new Label("Name:"), 0, 0);
         final TextField projectNameTextField = new TextField();
         projectNameTextField.setPromptText("Projectname");
         grid.add(projectNameTextField, 1, 0);

         grid.add(new Label("Color:"), 0, 1);
         final ColorPicker colorPicker = new ColorPicker();
         grid.add(colorPicker, 1, 1);

         grid.add(new Label("IsWork:"), 0, 2);
         final CheckBox isWorkCheckBox = new CheckBox();
         grid.add(isWorkCheckBox, 1, 2);
         // TODO disable OK button if no name is set
         dialog.getDialogPane().setContent(grid);

         dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
               return new Project(projectNameTextField.getText(), colorPicker.getValue(), isWorkCheckBox.isSelected());
            }
            return null;
         });
         mainStage.setAlwaysOnTop(false);
         final Optional<Project> result = dialog.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(project -> {
            controller.addNewProject(project.getName(), project.isWork(), project.getColor());
         });
      });

      settingsButton.setOnAction((ae) -> {
         Log.info("Settings clicked");
         this.mainStage.setAlwaysOnTop(false);
         settingsStage.show();
      });

      calendarButton.setOnAction((ae) -> {
         Log.info("Calendar clicked");
         this.mainStage.setAlwaysOnTop(false);
         reportStage.show();
      });

      Platform.runLater(() -> {
         projectSelectionNodeMap = new HashMap<>(model.availableProjects.size());
         for (final Project project : model.availableProjects) {
            if (project.isEnabled()) {
               final Node node = addProjectToProjectList(project);
               projectSelectionNodeMap.put(project, node);
            }
         }
         model.availableProjects.addListener((ListChangeListener<Project>) lis -> {
            while (lis.next()) {
               if (lis.wasAdded()) {
                  final List<? extends Project> addedSubList = lis.getAddedSubList();
                  for (final Project project : addedSubList) {
                     final Node node = addProjectToProjectList(project);
                     projectSelectionNodeMap.put(project, node);
                  }
               } else if (lis.wasRemoved()) {
                  final List<? extends Project> removedSubList = lis.getRemoved();
                  for (final Project project : removedSubList) {
                     // change to idle if removed project was active
                     if (project == model.activeWorkItem.get().getProject()) {
                        changeProject(model.idleProject, 0);
                     }
                     final Node remove = projectSelectionNodeMap.remove(project);
                     availableProjectVbox.getChildren().remove(remove);
                  }
               }
            }
         });
      });

      // Change color
      pane.setOnMouseEntered((a) -> {
         String style = changeStyleAttribute(pane.getStyle(), "fx-background-color", "rgba(54,143,179,.1)");
         style = changeStyleAttribute(style, "fx-border-color", "rgba(54,143,179,.3)");
         pane.setStyle(style);

         mouseHovering.set(true);
      });

      pane.setOnMouseExited((a) -> {
         String style = changeStyleAttribute(pane.getStyle(), "fx-background-color", "rgba(54,143,179,0.01)");
         style = changeStyleAttribute(style, "fx-border-color", "rgba(54,143,179,0)");
         pane.setStyle(style);
         mouseHovering.set(false);
      });

      // Drag stage
      pane.setOnMousePressed((mouseEvent) -> {
         // record a delta distance for the drag and drop operation.
         dragDelta.x = mainStage.getX() - mouseEvent.getScreenX();
         dragDelta.y = mainStage.getY() - mouseEvent.getScreenY();
         wasDragged = false;
      });

      pane.setOnMouseDragged((mouseEvent) -> {
         mainStage.setX(mouseEvent.getScreenX() + dragDelta.x);
         mainStage.setY(mouseEvent.getScreenY() + dragDelta.y);
         wasDragged = true;
      });

      Interval.registerCallBack(() -> {
         // update ui each second

         final LocalDateTime now = LocalDateTime.now();
         model.activeWorkItem.get().setEndTime(now); // TODO not good to change model

         final long currentWorkSeconds = Duration
               .between(model.activeWorkItem.get().getStartTime(), model.activeWorkItem.get().getEndTime())
               .getSeconds();
         final long todayWorkingSeconds = calcTodaysWorkSeconds();
         final long todaySeconds = calcTodaysSeconds();

         // update all ui labels
         // TODO do it with bindings (maybe create a viewmodel for this)
         bigTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentWorkSeconds));
         allTimeLabel.setText(DateFormatter.secondsToHHMMSS(todayWorkingSeconds));
         todayAllSeconds.setText(DateFormatter.secondsToHHMMSS(todaySeconds));

         // Test if needed hours are achieved
         if (todayWorkingSeconds > model.neededWorkSeconds) {
            allTimeLabel.setTextFill(Color.DARKGREEN);
         }

         for (final Project p : elapsedProjectTimeLabelMap.keySet()) {
            final Label label = elapsedProjectTimeLabelMap.get(p);

            final long seconds = model.pastWorkItems.stream().filter((work) -> work.getProject().getId() == p.getId())
                  .mapToLong(work -> {
                     return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
                  }).sum();
            label.setText(DateFormatter.secondsToHHMMSS(seconds));
         }

         updateProjectColorTimeline();
         updateTaskbarIcon(currentWorkSeconds);
      });

   }

   private void loadSubStages() throws IOException {
      // Report stage
      final FXMLLoader fxmlLoader = createFXMLLoader(RESOURCE.FXML_REPORT);
      final Parent sceneRoot = (Parent) fxmlLoader.load();
      reportController = fxmlLoader.getController();
      reportStage = new Stage();
      reportStage.initModality(Modality.APPLICATION_MODAL);
      reportStage.setScene(new Scene(sceneRoot));
      reportStage.setTitle("Report");
      reportStage.setOnCloseRequest(e -> {
         this.mainStage.setAlwaysOnTop(true);
      });

      // Settings stage
      final FXMLLoader fxmlLoader2 = createFXMLLoader(RESOURCE.FXML_SETTINGS);
      final Parent root1 = (Parent) fxmlLoader2.load();
      settingsController = fxmlLoader2.getController();

      settingsStage = new Stage();
      settingsStage.initModality(Modality.APPLICATION_MODAL);
      settingsStage.setTitle("Settings");
      settingsStage.setScene(new Scene(root1));
      settingsStage.setOnCloseRequest(e -> {
         this.mainStage.setAlwaysOnTop(true);
      });
   }

   private FXMLLoader createFXMLLoader(final RESOURCE fxmlLayout) {
      final FXMLLoader fxmlLoader = new FXMLLoader(Resources.getResource(fxmlLayout));

      return fxmlLoader;
   }

   private Node addProjectToProjectList(final Project p) {
      // context menu
      final ContextMenu contextMenu = new ContextMenu();

      final FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Resources.getResource(RESOURCE.FXML_PROJECT_LAYOUT));
      Pane projectElement;
      try {
         projectElement = (Pane) loader.load();
      } catch (final IOException e1) {
         Log.error("Could not load '{}'.", loader.getLocation(), e1);
         throw new RuntimeException(e1);
      }

      final Label projectNameLabel = (Label) projectElement.getChildren().get(0);
      final Label elapsedTimeLabel = (Label) projectElement.getChildren().get(1);
      elapsedProjectTimeLabelMap.put(p, elapsedTimeLabel);

      projectNameLabel.setText(p.getName());
      final Color color = p.getColor();
      final double dimFactor = .6;
      final Color dimmedColor = new Color(color.getRed() * dimFactor, color.getGreen() * dimFactor,
            color.getBlue() * dimFactor, 1);
      projectNameLabel.setTextFill(dimmedColor);

      projectNameLabel.setUserData(p);
      projectNameLabel.setOnContextMenuRequested(event -> {
         contextMenu.show(projectNameLabel, event.getScreenX(), event.getScreenY());
      });

      projectNameLabel.setOnMouseClicked((a) -> {
         if (wasDragged) {
            return;
         }
         // a.consume();
         final MouseButton button = a.getButton();
         if (button == MouseButton.PRIMARY) {
            changeProject(p, 0);
         } else if (button == MouseButton.SECONDARY) {

         }

      });
      projectNameLabel.setOnMouseEntered(ae -> {
         projectNameLabel.setTextFill(p.getColor());
         final Bloom bloom = new Bloom();
         bloom.setThreshold(0.3);
         projectNameLabel.setEffect(bloom);
         projectNameLabel.setUnderline(true);
      });
      projectNameLabel.setOnMouseExited(ae -> {
         projectNameLabel.setTextFill(new Color(p.getColor().getRed() * dimFactor, p.getColor().getGreen() * dimFactor,
               p.getColor().getBlue() * dimFactor, 1));
         projectNameLabel.setEffect(null);
         projectNameLabel.setUnderline(false);
      });

      availableProjectVbox.getChildren().add(projectElement);
      // TODO dialog modality
      final MenuItem changeWithTimeMenuItem = new MenuItem("Change with time");
      changeWithTimeMenuItem.setGraphic(new ImageView("file:volleyball.png"));
      changeWithTimeMenuItem.setOnAction(e -> {
         Log.info("Change with time");
         final Dialog<Integer> dialog = new Dialog<>();
         dialog.setTitle("Change project with time");
         dialog.setHeaderText("Choose the time to subtract");

         dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

         final GridPane grid = new GridPane();
         grid.setHgap(10);
         grid.setVgap(10);
         grid.setPadding(new Insets(20, 150, 10, 10));
         // TODO calc max values (difference now-start)
         grid.add(new Label("Hours:"), 0, 0);
         final Spinner<Integer> hoursSpinner = new Spinner<>(0, 100, 0);
         grid.add(hoursSpinner, 1, 0);

         grid.add(new Label("Minutes:"), 0, 1);
         final Spinner<Integer> minutesSpinner = new Spinner<>(0, 100, 0);
         grid.add(minutesSpinner, 1, 1);

         grid.add(new Label("Seconds:"), 0, 2);
         final Spinner<Integer> secondsSpiner = new Spinner<>(0, 100, 0);
         grid.add(secondsSpiner, 1, 2);

         grid.add(new Label("Offset:"), 0, 3);
         final Label offsetLabel = new Label("0 seconds");
         grid.add(offsetLabel, 1, 3);

         final IntegerBinding secondOffsetBinding = new IntegerBinding() {
            {
               super.bind(hoursSpinner.valueProperty(), minutesSpinner.valueProperty(), secondsSpiner.valueProperty());
            }

            @Override
            protected int computeValue() {
               return hoursSpinner.getValue() * 60 * 60 + minutesSpinner.getValue() * 60 + secondsSpiner.getValue();
            }
         };

         offsetLabel.textProperty().bind(secondOffsetBinding.asString());

         dialog.getDialogPane().setContent(grid);

         dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
               return secondOffsetBinding.get();
            }
            return null;
         });
         mainStage.setAlwaysOnTop(false);
         final Optional<Integer> result = dialog.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(minusSeconds -> {
            changeProject(p, minusSeconds);
         });
      });
      final MenuItem deleteMenuItem = new MenuItem("Delete");
      deleteMenuItem.setOnAction(e -> {
         Log.info("Delete");

         final Alert alert = new Alert(AlertType.WARNING);
         alert.setTitle("Delete project");
         alert.setHeaderText("Delete project '" + p.getName() + "'.");
         alert.setContentText("You sure?");

         mainStage.setAlwaysOnTop(false);
         final Optional<ButtonType> result = alert.showAndWait();
         mainStage.setAlwaysOnTop(true);

         if (result.get() == ButtonType.OK) {
            controller.deleteProject(p);
         }

      });

      final MenuItem editMenuItem = new MenuItem("Edit");
      editMenuItem.setOnAction(e -> {
         Log.info("Edit project");
         final Dialog<ButtonType> dialog = new Dialog<>();
         dialog.setTitle("Edit project");
         dialog.setHeaderText("Edit project '" + p.getName() + "'");

         dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

         final GridPane grid = new GridPane();
         grid.setHgap(10);
         grid.setVgap(10);
         grid.setPadding(new Insets(20, 150, 10, 10));

         grid.add(new Label("Name:"), 0, 0);
         final TextField projectNameTextField = new TextField(p.getName());
         grid.add(projectNameTextField, 1, 0);

         grid.add(new Label("Color:"), 0, 1);
         final ColorPicker colorPicker = new ColorPicker(p.getColor());
         grid.add(colorPicker, 1, 1);

         grid.add(new Label("IsWork:"), 0, 2);
         final CheckBox isWorkCheckBox = new CheckBox();
         isWorkCheckBox.setSelected(p.isWork());
         grid.add(isWorkCheckBox, 1, 2);
         // TODO disable OK button if no name is set
         dialog.getDialogPane().setContent(grid);

         dialog.setResultConverter(dialogButton -> {
            return dialogButton;
         });

         mainStage.setAlwaysOnTop(false);
         final Optional<ButtonType> result = dialog.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
               return;
            }
            controller.editProject(p, projectNameTextField.getText(), colorPicker.getValue(),
                  isWorkCheckBox.isSelected());

            projectNameLabel.setText(p.getName());
            projectNameLabel.setTextFill(new Color(p.getColor().getRed() * dimFactor,
                  p.getColor().getGreen() * dimFactor, p.getColor().getBlue() * dimFactor, 1));
         });
         updateProjectView();

      });

      // Add MenuItem to ContextMenu
      contextMenu.getItems().addAll(changeWithTimeMenuItem, editMenuItem, deleteMenuItem);

      return projectElement;
   }

   private void updateProjectColorTimeline() {
      final GraphicsContext gc = canvas.getGraphicsContext2D();

      gc.setFill(new Color(.3, .3, .3, .3));
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

      int currentX = 0;
      for (final Work w : model.pastWorkItems) {
         final long maxSeconds = 60 * 60 * 10;
         final long workedSeconds = Duration.between(w.getStartTime(), w.getEndTime()).getSeconds();
         final int fillX = (int) ((float) workedSeconds / maxSeconds * canvas.getWidth());
         gc.setFill(w.getProject().getColor());
         gc.fillRect(currentX, 0, fillX, canvas.getHeight());
         currentX += fillX;
      }
   }

   private void updateTaskbarIcon(final long currentWorkSeconds) {
      // update taskbar icon
      final GraphicsContext gcIcon = taskbarCanvas.getGraphicsContext2D();

      gcIcon.clearRect(0, 0, taskbarCanvas.getWidth(), taskbarCanvas.getHeight());
      gcIcon.setFill(model.activeWorkItem.get().getProject().getColor());
      gcIcon.fillRect(1, 27, 31, 5);

      gcIcon.setStroke(model.taskBarColor);
      gcIcon.setTextAlign(TextAlignment.CENTER);
      gcIcon.setFont(new Font("Arial", 12));
      gcIcon.strokeText(DateFormatter.secondsToHHMMSS(currentWorkSeconds).replaceFirst(":", ":\n"),
            Math.round(taskbarCanvas.getWidth() / 2), Math.round(taskbarCanvas.getHeight() / 2) - 5);

      final SnapshotParameters snapshotParameters = new SnapshotParameters();
      snapshotParameters.setFill(Color.TRANSPARENT);
      final WritableImage image = taskbarCanvas.snapshot(snapshotParameters, null);

      final StackPane layout = new StackPane();
      layout.getChildren().addAll(new ImageView(image));

      final BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
      final Image icon = SwingFXUtils.toFXImage(bi, null);

      final ObservableList<Image> icons = Main.stage.getIcons();
      icons.addAll(icon);
      if (icons.size() > 1) {
         icons.remove(0);
      }
   }

   private void updateProjectView() {
      currentProjectLabel.setText(model.activeWorkItem.get().getProject().getName());
      final Circle circle = new Circle(4);
      circle.setFill(model.activeWorkItem.get().getProject().getColor());
      currentProjectLabel.setGraphic(circle);
   }

   private long calcTodaysWorkSeconds() {
      return model.pastWorkItems.stream().filter((work) -> work.getProject().isWork()).mapToLong((work) -> {
         return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
      }).sum();
   }

   private long calcTodaysSeconds() {
      return model.pastWorkItems.stream().mapToLong((work) -> {
         return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
      }).sum();
   }

   public static String changeStyleAttribute(final String style, final String attribute, final String newValue) {

      String newStyle = "";
      // Log.info("Old style {}", style);
      final String newStyleAttribute = "-" + attribute + ": " + newValue + "; ";
      if (style.contains(attribute)) {
         // Log.info("replacing");
         newStyle = style.replaceAll("-" + attribute + ": " + "[^;]+;", newStyleAttribute);
      } else {
         // Log.info("adding");
         newStyle = style + newStyleAttribute;
      }

      // Log.info("new style: {}", newStyle);

      return newStyle;
   }

   Stage mainStage;

   public void setStage(final Stage primaryStage) {
      this.mainStage = primaryStage;
   }

}
