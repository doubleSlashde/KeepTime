package de.ds.keeptime.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.ds.keeptime.Main;
import de.ds.keeptime.common.ColorHelper;
import de.ds.keeptime.common.DateFormatter;
import de.ds.keeptime.common.Resources;
import de.ds.keeptime.common.Resources.RESOURCE;
import de.ds.keeptime.controller.Controller;
import de.ds.keeptime.model.Model;
import de.ds.keeptime.model.Project;
import de.ds.keeptime.model.Work;
import de.ds.keeptime.view.time.Interval;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
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
   private BorderPane borderPane;

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

   Controller controller;
   Model model;

   public void setController(final Controller controller, final Model model) {
      this.controller = controller;
      this.model = model;

      changeProject(model.idleProject, 0); // TODO initialize not here!
      updateProjectView();
   }

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   public void changeProject(final Project newProject, final long minusSeconds) {
      controller.changeProject(newProject, minusSeconds);
      updateProjectView();
      textArea.setText("");
   }

   Map<Project, Label> elapsedProjectTimeLabelMap = new HashMap<>();

   boolean wasDragged = false;

   Canvas taskbarCanvas = new Canvas(32, 32);

   BooleanProperty mouseHoveringProperty = new SimpleBooleanProperty(false);
   LongProperty activeWorkSecondsProperty = new SimpleLongProperty(0);
   ObjectProperty<Color> fontColorProperty = new SimpleObjectProperty<>();

   Stage reportStage;
   ReportController reportController;

   Stage settingsStage;
   SettingsController settingsController;

   Map<Project, Node> projectSelectionNodeMap;

   @FXML
   private void initialize() throws IOException {

      bigTimeLabel.setText("00:00:00");
      allTimeLabel.setText("00:00:00");
      todayAllSeconds.setText("00:00:00");

      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.editableProperty().bind(mouseHoveringProperty);

      textArea.textProperty().addListener((a, b, c) -> {
         controller.setComment(textArea.getText());
      });

      // reposition window if projects are hidden (as anchor is top left)
      mouseHoveringProperty.addListener((a, b, c) -> {
         // TODO fix the not so nice jumping..
         projectsVBox.setManaged(c);
         final double beforeWidth = Main.stage.getWidth();
         Main.stage.sizeToScene();
         final double afterWidth = Main.stage.getWidth();
         projectsVBox.setVisible(c);
         final double offset = afterWidth - beforeWidth;
         if (!model.displayProjectsRight.get()) {
            // we only need to move the stage if the node on the left is hidden
            Main.stage.setX(Main.stage.getX() - offset);
         }
      });

      minimizeButton.setOnAction((ae) -> {
         Main.stage.setIconified(true);
      });
      minimizeButton.textFillProperty().bind(fontColorProperty);
      closeButton.setOnAction((ae) -> {
         Main.stage.close();
      });
      closeButton.textFillProperty().bind(fontColorProperty);

      addNewProjectButton.textFillProperty().bind(fontColorProperty);
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

         grid.add(new Label("SortIndex:"), 0, 3);
         final Spinner<Integer> indexSpinner = new Spinner<>();
         final int availableProjectAmount = model.availableProjects.size();
         indexSpinner
               .setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
         grid.add(indexSpinner, 1, 3);
         // TODO disable OK button if no name is set
         dialog.getDialogPane().setContent(grid);

         dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
               return new Project(projectNameTextField.getText(), colorPicker.getValue(), isWorkCheckBox.isSelected(),
                     indexSpinner.getValue()); // temporary (misused) transfer object for project
            }
            return null;
         });
         mainStage.setAlwaysOnTop(false);
         final Optional<Project> result = dialog.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(project -> {
            controller.addNewProject(project.getName(), project.isWork(), project.getColor(), project.getIndex());
            realignProjectList();
         });
      });

      // Add a light to colorize buttons
      // TODO does this go nicer?
      final Lighting lighting = new Lighting();
      lighting.lightProperty().bind(Bindings.createObjectBinding(() -> {
         final Color color = fontColorProperty.get();
         return new Light.Distant(45, 45, color);
      }, fontColorProperty));

      settingsButton.setOnAction((ae) -> {
         Log.info("Settings clicked");
         this.mainStage.setAlwaysOnTop(false);
         settingsController.update();
         settingsStage.show();
      });
      settingsButton.setEffect(lighting);

      calendarButton.setOnAction((ae) -> {
         Log.info("Calendar clicked");
         this.mainStage.setAlwaysOnTop(false);
         reportController.update();
         reportStage.show();
      });
      calendarButton.setEffect(lighting);

      final Runnable updateMainBackgroundColor = () -> {
         Color color = model.defaultBackgroundColor.get();
         double opacity = 0;
         if (mouseHoveringProperty.get()) {
            color = model.hoverBackgroundColor.get();
            opacity = .3;
         }
         String style = changeStyleAttribute(pane.getStyle(), "fx-background-color",
               "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
         style = changeStyleAttribute(style, "fx-border-color",
               "rgba(" + ColorHelper.colorToCssRgb(color) + ", " + opacity + ")");
         pane.setStyle(style);
      };

      mouseHoveringProperty.addListener((a, b, c) -> {
         updateMainBackgroundColor.run();
      });

      Platform.runLater(() -> {
         loadSubStages();
         fontColorProperty.set(model.defaultFontColor.get());
         fontColorProperty.bind(Bindings.createObjectBinding(() -> {
            if (mouseHoveringProperty.get()) {
               return model.hoverFontColor.get();
            } else {
               return model.defaultFontColor.get();
            }
         }, mouseHoveringProperty, model.defaultFontColor, model.hoverFontColor));

         bigTimeLabel.textFillProperty().bind(fontColorProperty);
         allTimeLabel.textFillProperty().bind(fontColorProperty);
         todayAllSeconds.textFillProperty().bind(fontColorProperty);
         currentProjectLabel.textFillProperty().bind(fontColorProperty);

         final Runnable displayProjectRightRunnable = () -> {
            if (model.displayProjectsRight.get()) {
               borderPane.setLeft(null);
               borderPane.setRight(projectsVBox);
            } else {
               borderPane.setRight(null);
               borderPane.setLeft(projectsVBox);

            }
         };
         model.displayProjectsRight.addListener((a, oldValue, newValue) -> {
            displayProjectRightRunnable.run();
         });
         displayProjectRightRunnable.run();

         // Setup textarea font color binding
         final Runnable textAreaColorRunnable = () -> {
            final String textAreaStyle = changeStyleAttribute(textArea.getStyle(), "fx-text-fill",
                  "rgba(" + ColorHelper.colorToCssRgba(fontColorProperty.get()) + ")");
            textArea.setStyle(textAreaStyle);
         };
         fontColorProperty.addListener((a, b, c) -> {
            textAreaColorRunnable.run();
         });
         textAreaColorRunnable.run();

         projectSelectionNodeMap = new HashMap<>(model.availableProjects.size());

         for (final Project project : model.sortedAvailableProjects) {
            if (project.isEnabled()) {
               final Node node = addProjectToProjectList(project);
               projectSelectionNodeMap.put(project, node);
            }
         }

         model.activeWorkItem.addListener((a, b, c) -> {
            updateProjectView();
            textArea.setText("");
         });

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

         model.defaultBackgroundColor.addListener((a, b, c) -> {
            updateMainBackgroundColor.run();
         });
         model.hoverBackgroundColor.addListener((a, b, c) -> {
            updateMainBackgroundColor.run();
         });
         updateMainBackgroundColor.run();
      });

      pane.setOnMouseEntered((a) -> {
         mouseHoveringProperty.set(true);
      });

      pane.setOnMouseExited((a) -> {
         mouseHoveringProperty.set(false);
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

      bigTimeLabel.textProperty().bind(Bindings.createStringBinding(() -> {
         return DateFormatter.secondsToHHMMSS(activeWorkSecondsProperty.get());
      }, activeWorkSecondsProperty));

      // update ui each second
      Interval.registerCallBack(() -> {

         final LocalDateTime now = LocalDateTime.now();
         model.activeWorkItem.get().setEndTime(now); // TODO not good to change model

         final long currentWorkSeconds = Duration
               .between(model.activeWorkItem.get().getStartTime(), model.activeWorkItem.get().getEndTime())
               .getSeconds();
         activeWorkSecondsProperty.set(currentWorkSeconds);
         final long todayWorkingSeconds = controller.calcTodaysWorkSeconds();
         final long todaySeconds = controller.calcTodaysSeconds();

         // update all ui labels
         // TODO do it with bindings (maybe create a viewmodel for this)
         // bigTimeLabel.setText(DateFormatter.secondsToHHMMSS(currentWorkSeconds));
         allTimeLabel.setText(DateFormatter.secondsToHHMMSS(todayWorkingSeconds));
         todayAllSeconds.setText(DateFormatter.secondsToHHMMSS(todaySeconds));

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

   private void loadSubStages() {
      try {
         // Report stage
         final FXMLLoader fxmlLoader = createFXMLLoader(RESOURCE.FXML_REPORT);
         final Parent sceneRoot = (Parent) fxmlLoader.load();
         reportController = fxmlLoader.getController();
         reportStage = new Stage();
         reportStage.initModality(Modality.APPLICATION_MODAL);
         reportStage.setScene(new Scene(sceneRoot));
         reportStage.setTitle("Report");
         reportStage.setResizable(false);
         reportStage.setOnHiding(e -> {
            this.mainStage.setAlwaysOnTop(true);
         });

         // Settings stage
         final FXMLLoader fxmlLoader2 = createFXMLLoader(RESOURCE.FXML_SETTINGS);
         final Parent root1 = (Parent) fxmlLoader2.load();
         settingsController = fxmlLoader2.getController();
         settingsController.setModelAndController(model, controller);
         settingsStage = new Stage();
         settingsController.setStage(settingsStage);
         settingsStage.initModality(Modality.APPLICATION_MODAL);
         settingsStage.setTitle("Settings");
         settingsStage.setResizable(false);
         settingsStage.setScene(new Scene(root1));
         settingsStage.setOnHiding(e -> {
            this.mainStage.setAlwaysOnTop(true);
         });
      } catch (final IOException e) {
         throw new RuntimeException(e);
      }
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
      elapsedTimeLabel.textFillProperty().bind(fontColorProperty);
      elapsedProjectTimeLabelMap.put(p, elapsedTimeLabel);

      projectNameLabel.setText(p.getName());
      projectNameLabel.setUnderline(p.isWork());
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
         // projectNameLabel.setUnderline(true);
      });
      projectNameLabel.setOnMouseExited(ae -> {
         projectNameLabel.setTextFill(new Color(p.getColor().getRed() * dimFactor, p.getColor().getGreen() * dimFactor,
               p.getColor().getBlue() * dimFactor, 1));
         projectNameLabel.setEffect(null);
         // projectNameLabel.setUnderline(false);
      });

      availableProjectVbox.getChildren().add(projectElement);
      // TODO dialog modality
      final MenuItem changeWithTimeMenuItem = new MenuItem("Change with time");
      changeWithTimeMenuItem.setOnAction(e -> {
         Log.info("Change with time");
         final Dialog<Integer> dialog = new Dialog<>();
         dialog.setTitle("Change project with time transfer");
         dialog.setHeaderText("Choose the time to transfer");
         dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

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
         grid.add(slider, 1, gridRow);
         final Label minutesToTransferLabel = new Label("999 minute(s)");
         grid.add(minutesToTransferLabel, 2, gridRow);
         gridRow++;

         grid.add(new Label("New time distribution"), 0, gridRow);
         gridRow++;
         grid.add(new Label("Active project duration: " + model.activeWorkItem.get().getProject().getName()), 0,
               gridRow);
         final Label currentProjectTimeLabel = new Label("00:00:00");
         grid.add(currentProjectTimeLabel, 1, gridRow);
         gridRow++;

         grid.add(new Label("New end and start time:"), 0, gridRow);
         final Label newEndTimeLabel = new Label("00:00:00");
         grid.add(newEndTimeLabel, 1, gridRow);
         gridRow++;

         grid.add(new Label("New project duration: " + p.getName()), 0, gridRow);
         final Label newProjectTimeLabel = new Label("00:00:00");
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
         activeWorkSecondsProperty.addListener((obs, oldValue, newValue) -> {
            updateLabelsRunnable.run();
         });
         slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateLabelsRunnable.run();
         });
         vBox.getChildren().add(grid);

         dialog.getDialogPane().setContent(vBox);

         dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
               return slider.valueProperty().intValue() * 60;
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
      deleteMenuItem.setDisable(p.isDefault());
      deleteMenuItem.setOnAction(e -> {
         Log.info("Delete");

         final Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Delete project");
         alert.setHeaderText("Delete project '" + p.getName() + "'.");
         alert.setContentText(
               "The project will just be hidden from display, as there may be work references to this project.");

         mainStage.setAlwaysOnTop(false);
         final Optional<ButtonType> result = alert.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(res -> {
            if (result.get() == ButtonType.OK) {
               controller.deleteProject(p);
               realignProjectList();
            }
         });
      });

      final MenuItem editMenuItem = new MenuItem("Edit");
      editMenuItem.setOnAction(e -> {
         // TODO refactor to use "add project" controls
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

         grid.add(new Label("SortIndex:"), 0, 3);
         final Spinner<Integer> indexSpinner = new Spinner<>();
         final int availableProjectAmount = model.availableProjects.size();
         indexSpinner.setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount - 1, p.getIndex()));
         grid.add(indexSpinner, 1, 3);

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
                  isWorkCheckBox.isSelected(), indexSpinner.getValue());

            projectNameLabel.setText(p.getName());
            projectNameLabel.setTextFill(new Color(p.getColor().getRed() * dimFactor,
                  p.getColor().getGreen() * dimFactor, p.getColor().getBlue() * dimFactor, 1));
            projectNameLabel.setUnderline(p.isWork());

            updateProjectView();
            realignProjectList();
         });
      });

      // Add MenuItem to ContextMenu
      contextMenu.getItems().addAll(changeWithTimeMenuItem, editMenuItem, deleteMenuItem);

      return projectElement;
   }

   private void realignProjectList() {
      Log.debug("Sorting project view");
      final ObservableList<Node> children = availableProjectVbox.getChildren();
      children.clear();
      // TODO changing the model is not ok from here, but the list is not resorted
      final Comparator<? super Project> comparator = model.sortedAvailableProjects.getComparator();
      model.sortedAvailableProjects.setComparator(null); // TODO is there a better way?
      model.sortedAvailableProjects.setComparator(comparator);
      for (final Project p : model.sortedAvailableProjects) {
         children.add(projectSelectionNodeMap.get(p));
      }
   }

   private void updateProjectColorTimeline() {
      final GraphicsContext gc = canvas.getGraphicsContext2D();

      gc.setFill(new Color(.3, .3, .3, .3));
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

      int currentX = 0;
      final long maxSeconds = controller.calcTodaysSeconds();// 60 * 60 * 10;
      for (final Work w : model.pastWorkItems) {
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

      gcIcon.setStroke(model.taskBarColor.get());
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
      final Project project = model.activeWorkItem.get().getProject();
      currentProjectLabel.setText(project.getName());
      currentProjectLabel.setUnderline(project.isWork());
      final Circle circle = new Circle(4);
      circle.setFill(project.getColor());
      currentProjectLabel.setGraphic(circle);
   }

   public static String changeStyleAttribute(final String style, final String attribute, final String newValue) {
      String newStyle = "";
      final String newStyleAttribute = "-" + attribute + ": " + newValue + "; ";
      if (style.contains(attribute)) {
         newStyle = style.replaceAll("-" + attribute + ": " + "[^;]+;", newStyleAttribute);
      } else {
         newStyle = style + newStyleAttribute;
      }

      return newStyle;
   }

   Stage mainStage;

   public void setStage(final Stage primaryStage) {
      this.mainStage = primaryStage;
   }

}
