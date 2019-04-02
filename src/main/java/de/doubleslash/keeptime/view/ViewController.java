// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.common.ColorHelper;
import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.FontProvider;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.view.time.Interval;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Component
public class ViewController {
   private static final String TIME_ZERO = "00:00:00";

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

   private static final Logger LOG = LoggerFactory.getLogger(ViewController.class);

   private class Delta {
      double x;
      double y;
   }

   private final Delta dragDelta = new Delta();

   private Stage mainStage;
   private Controller controller;
   private Model model;

   public void setController(final Controller controller, final Model model) {
      this.controller = controller;
      this.model = model;

      changeProject(model.getIdleProject(), 0); // TODO initialize not here!
      updateProjectView();
   }

   public void changeProject(final Project newProject, final long minusSeconds) {
      controller.changeProject(newProject, minusSeconds);
      updateProjectView();
      textArea.setText("");
   }

   private final Map<Project, Label> elapsedProjectTimeLabelMap = new HashMap<>();

   private boolean wasDragged = false;

   private final Canvas taskbarCanvas = new Canvas(32, 32);

   private final BooleanProperty mouseHoveringProperty = new SimpleBooleanProperty(false);
   private final LongProperty activeWorkSecondsProperty = new SimpleLongProperty(0);
   private final ObjectProperty<Color> fontColorProperty = new SimpleObjectProperty<>();

   private Stage reportStage;
   private ReportController reportController;

   private Stage settingsStage;
   private SettingsController settingsController;

   private Stage aboutStage;
   private AboutController aboutController;

   private Map<Project, Node> projectSelectionNodeMap;

   @FXML
   private void initialize() {

      setUpTime();

      setUpTextArea();

      // reposition window if projects are hidden (as anchor is top left)
      mouseHoveringProperty.addListener((a, b, c) -> {
         if (!Model.HIDE_PROJECTS_ON_MOUSE_EXIT.get()) {
            setProjectListVisible(true);
            return;
         }

         setProjectListVisible(c);
      });

      minimizeButton.setOnAction(ae -> mainStage.setIconified(true));
      minimizeButton.textFillProperty().bind(fontColorProperty);
      closeButton.setOnAction(ae -> mainStage.close());
      closeButton.textFillProperty().bind(fontColorProperty);

      addNewProjectButton.textFillProperty().bind(fontColorProperty);

      // Add a light to colorize buttons
      // TODO does this go nicer?
      final Lighting lighting = new Lighting();
      lighting.lightProperty().bind(Bindings.createObjectBinding(() -> {
         final Color color = fontColorProperty.get();
         return new Light.Distant(45, 45, color);
      }, fontColorProperty));

      settingsButton.setOnAction(ae -> settingsClicked());
      settingsButton.setEffect(lighting);

      calendarButton.setOnAction(ae -> calendarClicked());
      calendarButton.setEffect(lighting);

      final Runnable updateMainBackgroundColor = this::runUpdateMainBackgroundColor;

      mouseHoveringProperty.addListener((a, b, c) -> updateMainBackgroundColor.run());

      Platform.runLater(() -> {
         loadSubStages();
         fontColorProperty.set(Model.DEFAULT_FONT_COLOR.get());
         fontColorProperty.bind(Bindings.createObjectBinding(() -> {
            if (mouseHoveringProperty.get()) {
               return Model.HOVER_FONT_COLOR.get();
            } else {
               return Model.DEFAULT_FONT_COLOR.get();
            }
         }, mouseHoveringProperty, Model.DEFAULT_FONT_COLOR, Model.HOVER_FONT_COLOR));

         bigTimeLabel.textFillProperty().bind(fontColorProperty);
         allTimeLabel.textFillProperty().bind(fontColorProperty);
         todayAllSeconds.textFillProperty().bind(fontColorProperty);
         currentProjectLabel.textFillProperty().bind(fontColorProperty);

         final Runnable displayProjectRightRunnable = () -> {
            if (Model.DISPLAY_PROJECTS_RIGHT.get()) {
               borderPane.setLeft(null);
               borderPane.setRight(projectsVBox);
            } else {
               borderPane.setRight(null);
               borderPane.setLeft(projectsVBox);

            }
         };
         Model.DISPLAY_PROJECTS_RIGHT.addListener((a, oldValue, newValue) -> displayProjectRightRunnable.run());
         displayProjectRightRunnable.run();

         // Setup textarea font color binding
         final Runnable textAreaColorRunnable = () -> {
            final String textAreaStyle = changeStyleAttribute(textArea.getStyle(), "fx-text-fill",
                  "rgba(" + ColorHelper.colorToCssRgba(fontColorProperty.get()) + ")");
            textArea.setStyle(textAreaStyle);
         };
         fontColorProperty.addListener((a, b, c) -> textAreaColorRunnable.run());
         textAreaColorRunnable.run();

         projectSelectionNodeMap = new HashMap<>(model.getAvailableProjects().size());

         for (final Project project : model.getSortedAvailableProjects()) {
            if (project.isEnabled()) {
               final Node node = addProjectToProjectList(project);
               projectSelectionNodeMap.put(project, node);
            }
         }

         Model.activeWorkItem.addListener((a, b, c) -> {
            updateProjectView();
            textArea.setText("");
         });

         model.getAvailableProjects().addListener(this::setUpAvailableProjectsListener);

         Model.DEFAULT_BACKGROUND_COLOR.addListener((a, b, c) -> updateMainBackgroundColor.run());
         Model.HOVER_BACKGROUND_COLOR.addListener((a, b, c) -> updateMainBackgroundColor.run());
         updateMainBackgroundColor.run();
      });

      pane.setOnMouseEntered(a -> mouseHoveringProperty.set(true));

      pane.setOnMouseExited(a -> mouseHoveringProperty.set(false));

      // Drag stage
      pane.setOnMousePressed(mouseEvent -> {
         // record a delta distance for the drag and drop operation.
         dragDelta.x = mainStage.getX() - mouseEvent.getScreenX();
         dragDelta.y = mainStage.getY() - mouseEvent.getScreenY();
         wasDragged = false;
      });

      pane.setOnMouseDragged(mouseEvent -> {
         mainStage.setX(mouseEvent.getScreenX() + dragDelta.x);
         mainStage.setY(mouseEvent.getScreenY() + dragDelta.y);
         wasDragged = true;
      });

      bigTimeLabel.textProperty().bind(Bindings.createStringBinding(
            () -> DateFormatter.secondsToHHMMSS(activeWorkSecondsProperty.get()), activeWorkSecondsProperty));

      // update ui each second
      Interval.registerCallBack(() -> {
         final LocalDateTime now = LocalDateTime.now();
         model.activeWorkItem.get().setEndTime(now); // FIXME not good to change model

         final long currentWorkSeconds = Duration
               .between(model.activeWorkItem.get().getStartTime(), model.activeWorkItem.get().getEndTime())
               .getSeconds();
         activeWorkSecondsProperty.set(currentWorkSeconds);
         final long todayWorkingSeconds = controller.calcTodaysWorkSeconds();
         final long todaySeconds = controller.calcTodaysSeconds();

         // update all ui labels
         allTimeLabel.setText(DateFormatter.secondsToHHMMSS(todayWorkingSeconds));
         todayAllSeconds.setText(DateFormatter.secondsToHHMMSS(todaySeconds));

         for (final Entry<Project, Label> entry : elapsedProjectTimeLabelMap.entrySet()) {
            final Project p = entry.getKey();
            final Label label = entry.getValue();

            final long seconds = model.getPastWorkItems().stream()
                  .filter((work) -> work.getProject().getId() == p.getId()).mapToLong(work -> {
                     return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
                  }).sum();
            label.setText(DateFormatter.secondsToHHMMSS(seconds));
         }

         updateProjectColorTimeline();
         updateTaskbarIcon(currentWorkSeconds);
      });

   }

   private Dialog<Project> dialogResultConverter(final Dialog<Project> dialog, final GridPane grid) {
      dialog.setResultConverter(dialogButton -> {
         if (dialogButton == ButtonType.OK) {
            final ObservableList<Node> nodes = grid.getChildren();
            final TextField projectNameTextField = (TextField) nodes.get(1);
            final ColorPicker colorPicker = (ColorPicker) nodes.get(3);
            final CheckBox isWorkCheckBox = (CheckBox) nodes.get(5);
            final Spinner<Integer> indexSpinner = (Spinner<Integer>) nodes.get(7);
            return new Project(projectNameTextField.getText(), colorPicker.getValue(), isWorkCheckBox.isSelected(),
                  indexSpinner.getValue()); // temporary (misused) transfer object for project
         }
         return null;
      });
      return dialog;
   }

   private void settingsClicked() {
      LOG.info("Settings clicked");
      this.mainStage.setAlwaysOnTop(false);
      settingsController.update();
      settingsStage.show();
   }

   private void calendarClicked() {
      LOG.info("Calendar clicked");
      this.mainStage.setAlwaysOnTop(false);
      reportController.update();
      reportStage.show();
   }

   private void runUpdateMainBackgroundColor() {
      Color color = Model.DEFAULT_BACKGROUND_COLOR.get();
      double opacity = 0;
      if (mouseHoveringProperty.get()) {
         color = Model.HOVER_BACKGROUND_COLOR.get();
         opacity = .3;
      }
      String style = changeStyleAttribute(pane.getStyle(), "fx-background-color",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      style = changeStyleAttribute(style, "fx-border-color",
            "rgba(" + ColorHelper.colorToCssRgb(color) + ", " + opacity + ")");
      pane.setStyle(style);
   }

   private void setUpAvailableProjectsListener(final Change<? extends Project> lis) {
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
               if (project == Model.activeWorkItem.get().getProject()) {
                  changeProject(model.getIdleProject(), 0);
               }
               final Node remove = projectSelectionNodeMap.remove(project);
               availableProjectVbox.getChildren().remove(remove);
            }
         }
      }
   }

   private void setUpTime() {
      bigTimeLabel.setText(TIME_ZERO);
      allTimeLabel.setText(TIME_ZERO);
      todayAllSeconds.setText(TIME_ZERO);
   }

   private void setUpTextArea() {
      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.editableProperty().bind(mouseHoveringProperty);

      textArea.textProperty().addListener((a, b, c) -> controller.setComment(textArea.getText()));
   }

   private void setProjectListVisible(final boolean showProjectList) {
      projectsVBox.setManaged(showProjectList);
      final double beforeWidth = mainStage.getWidth();
      mainStage.sizeToScene();
      final double afterWidth = mainStage.getWidth();
      projectsVBox.setVisible(showProjectList);
      final double offset = afterWidth - beforeWidth;
      if (!Model.DISPLAY_PROJECTS_RIGHT.get()) {
         // we only need to move the stage if the node on the left is hidden
         // not sure how we can prevent the jumping
         mainStage.setX(mainStage.getX() - offset);
      }
   }

   private void loadSubStages() {
      try {
         // Report stage
         final FXMLLoader fxmlLoader = createFXMLLoader(RESOURCE.FXML_REPORT);
         final Parent sceneRoot = fxmlLoader.load();
         reportController = fxmlLoader.getController();
         reportController.setModel(model);
         reportStage = new Stage();
         reportStage.initModality(Modality.APPLICATION_MODAL);
         reportStage.setScene(new Scene(sceneRoot));
         reportStage.setTitle("Report");
         reportStage.setResizable(false);
         reportStage.setOnHiding(e -> this.mainStage.setAlwaysOnTop(true));

         // Settings stage
         final FXMLLoader fxmlLoader2 = createFXMLLoader(RESOURCE.FXML_SETTINGS);
         final Parent root1 = fxmlLoader2.load();
         settingsController = fxmlLoader2.getController();
         settingsController.setController(controller);
         settingsStage = new Stage();
         settingsController.setStage(settingsStage);
         settingsStage.initModality(Modality.APPLICATION_MODAL);
         settingsStage.setTitle("Settings");
         settingsStage.setResizable(false);
         settingsStage.setScene(new Scene(root1));
         settingsStage.setOnHiding(e -> this.mainStage.setAlwaysOnTop(true));
      } catch (final IOException e) {
         LOG.error("Error while loading sub stage");
         throw new FXMLLoaderException(e);
      }
   }

   private FXMLLoader createFXMLLoader(final RESOURCE fxmlLayout) {
      return new FXMLLoader(Resources.getResource(fxmlLayout));
   }

   private Node addProjectToProjectList(final Project p) {
      // context menu
      final ContextMenu contextMenu = new ContextMenu();

      final FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Resources.getResource(RESOURCE.FXML_PROJECT_LAYOUT));
      Pane projectElement;
      try {
         projectElement = loader.load();
      } catch (final IOException e1) {
         LOG.error("Could not load '{}'.", loader.getLocation(), e1);
         throw new FXMLLoaderException(e1);
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
      projectNameLabel.setOnContextMenuRequested(
            event -> contextMenu.show(projectNameLabel, event.getScreenX(), event.getScreenY()));

      projectNameLabel.setOnMouseClicked(a -> {
         if (wasDragged) {
            return;
         }

         final MouseButton button = a.getButton();
         if (button == MouseButton.PRIMARY) {
            changeProject(p, 0);
         }

      });
      projectNameLabel.setOnMouseEntered(ae -> {
         projectNameLabel.setTextFill(p.getColor());
         final Bloom bloom = new Bloom();
         bloom.setThreshold(0.3);
         projectNameLabel.setEffect(bloom);
      });
      projectNameLabel.setOnMouseExited(ae -> {
         projectNameLabel.setTextFill(new Color(p.getColor().getRed() * dimFactor, p.getColor().getGreen() * dimFactor,
               p.getColor().getBlue() * dimFactor, 1));
         projectNameLabel.setEffect(null);
      });

      availableProjectVbox.getChildren().add(projectElement);

      final MenuItem changeWithTimeMenuItem = new MenuItem("Change with time");
      changeWithTimeMenuItem.setOnAction(e -> {
         LOG.info("Change with time");
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
         final Slider slider = setUpSliderChangeWithTimeMenuItem(activeWorkSecondsProperty);

         grid.add(slider, 1, gridRow);
         final Label minutesToTransferLabel = new Label("999 minute(s)");
         grid.add(minutesToTransferLabel, 2, gridRow);
         gridRow++;

         grid.add(new Label("New time distribution"), 0, gridRow);
         gridRow++;
         grid.add(new Label("Active project duration: " + Model.activeWorkItem.get().getProject().getName()), 0,
               gridRow);
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
                  DateFormatter.toTimeString(Model.activeWorkItem.get().getEndTime().minusSeconds(secondsOffset)));
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
         mainStage.setAlwaysOnTop(false);
         final Optional<Integer> result = dialog.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(minusSeconds -> changeProject(p, minusSeconds));
      });
      final MenuItem deleteMenuItem = new MenuItem("Delete");
      deleteMenuItem.setDisable(p.isDefault());
      deleteMenuItem.setOnAction(e -> {
         LOG.info("Delete");

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
         LOG.info("Edit project");
         final Dialog<ButtonType> dialog = setUpDialogButtonType("Edit project", "Edit project '" + p.getName() + "'");
         final GridPane grid = setUpEditProjectGridPane(p);

         // TODO disable OK button if no name is set
         dialog.getDialogPane().setContent(grid);

         dialog.setResultConverter(dialogButton -> dialogButton);

         mainStage.setAlwaysOnTop(false);
         final Optional<ButtonType> result = dialog.showAndWait();
         mainStage.setAlwaysOnTop(true);

         result.ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
               return;
            }
            final ObservableList<Node> nodes = grid.getChildren();
            editProject(nodes, p);

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

      return slider;
   }

   private void editProject(final ObservableList<Node> nodes, final Project p) {
      final TextField projectNameTextField = (TextField) nodes.get(1);
      final ColorPicker colorPicker = (ColorPicker) nodes.get(3);
      final CheckBox isWorkCheckBox = (CheckBox) nodes.get(5);
      final Spinner<Integer> indexSpinner = (Spinner<Integer>) nodes.get(7);
      controller.editProject(p, projectNameTextField.getText(), colorPicker.getValue(), isWorkCheckBox.isSelected(),
            indexSpinner.getValue());
   }

   private Dialog<ButtonType> setUpDialogButtonType(final String title, final String headerText) {
      final Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle(title);
      dialog.setHeaderText(headerText);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      return dialog;
   }

   private Dialog<Project> setUpDialogProject(final String title, final String headerText) {
      final Dialog<Project> dialog = new Dialog<>();
      dialog.setTitle(title);
      dialog.setHeaderText(headerText);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      return dialog;
   }

   private GridPane setUpEditProjectGridPane(final Project p) {
      final GridPane grid = setUpGridPane(p.getName(), p.getColor(), p.isWork());

      final Spinner<Integer> indexSpinner = new Spinner<>();
      final int availableProjectAmount = model.getAvailableProjects().size();
      indexSpinner.setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount - 1, p.getIndex()));
      grid.add(indexSpinner, 1, 3);

      return grid;
   }

   private GridPane setUpAddNewProjectGridPane(final String projectName, final Color projectColor,
         final boolean isWork) {
      final GridPane grid = setUpGridPane(projectName, projectColor, isWork);

      final Spinner<Integer> indexSpinner = new Spinner<>();
      final int availableProjectAmount = model.getAvailableProjects().size();
      indexSpinner.setValueFactory(new IntegerSpinnerValueFactory(0, availableProjectAmount, availableProjectAmount));
      grid.add(indexSpinner, 1, 3);

      return grid;
   }

   private GridPane setUpGridPane(final String projectName, final Color projectColor, final boolean isWork) {
      final GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      final Label nameLabel = new Label("Name:");
      nameLabel.setFont(FontProvider.getDefaultFont());
      grid.add(nameLabel, 0, 0);

      final TextField projectNameTextField = new TextField(projectName);
      projectNameTextField.setFont(FontProvider.getDefaultFont());
      grid.add(projectNameTextField, 1, 0);

      final Label colorLabel = new Label("Color:");
      colorLabel.setFont(FontProvider.getDefaultFont());
      grid.add(colorLabel, 0, 1);

      final ColorPicker colorPicker = new ColorPicker(projectColor);
      grid.add(colorPicker, 1, 1);

      final Label isWorkLabel = new Label("IsWork:");
      isWorkLabel.setFont(FontProvider.getDefaultFont());
      grid.add(isWorkLabel, 0, 2);

      final CheckBox isWorkCheckBox = new CheckBox();
      isWorkCheckBox.setSelected(isWork);
      isWorkCheckBox.setFont(FontProvider.getDefaultFont());
      grid.add(isWorkCheckBox, 1, 2);

      final Label sortIndex = new Label("SortIndex:");
      sortIndex.setFont(FontProvider.getDefaultFont());
      grid.add(new Label("SortIndex:"), 0, 3);

      return grid;
   }

   private void realignProjectList() {
      LOG.debug("Sorting project view");
      final ObservableList<Node> children = availableProjectVbox.getChildren();
      children.clear();
      // TODO changing the model is not ok from here, but the list is not resorted
      final Comparator<? super Project> comparator = model.getSortedAvailableProjects().getComparator();
      model.getSortedAvailableProjects().setComparator(null); // TODO is there a better way?
      model.getSortedAvailableProjects().setComparator(comparator);
      for (final Project p : model.getSortedAvailableProjects()) {
         children.add(projectSelectionNodeMap.get(p));
      }
   }

   private void updateProjectColorTimeline() {
      final GraphicsContext gc = canvas.getGraphicsContext2D();

      gc.setFill(new Color(.3, .3, .3, .3));
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

      double currentX = 0;
      final long maxSeconds = controller.calcTodaysSeconds();
      for (final Work w : model.getPastWorkItems()) {
         final long workedSeconds = Duration.between(w.getStartTime(), w.getEndTime()).getSeconds();
         final double fillX = (float) workedSeconds / maxSeconds * canvas.getWidth();
         gc.setFill(w.getProject().getColor());
         gc.fillRect(currentX, 0, fillX, canvas.getHeight());
         currentX += fillX;
      }
   }

   private void updateTaskbarIcon(final long currentWorkSeconds) {
      final GraphicsContext gcIcon = taskbarCanvas.getGraphicsContext2D();

      gcIcon.clearRect(0, 0, taskbarCanvas.getWidth(), taskbarCanvas.getHeight());
      gcIcon.setFill(Model.activeWorkItem.get().getProject().getColor());
      gcIcon.fillRect(1, 27, 31, 5);

      gcIcon.setStroke(Model.TASK_BAR_COLOR.get());
      gcIcon.setTextAlign(TextAlignment.CENTER);
      gcIcon.strokeText(DateFormatter.secondsToHHMMSS(currentWorkSeconds).replaceFirst(":", ":\n"),
            Math.round(taskbarCanvas.getWidth() / 2), Math.round(taskbarCanvas.getHeight() / 2) - 5.0);

      final SnapshotParameters snapshotParameters = new SnapshotParameters();
      snapshotParameters.setFill(Color.TRANSPARENT);
      final WritableImage image = taskbarCanvas.snapshot(snapshotParameters, null);

      final StackPane layout = new StackPane();
      layout.getChildren().addAll(new ImageView(image));

      final BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
      final Image icon = SwingFXUtils.toFXImage(bi, null);

      final ObservableList<Image> icons = mainStage.getIcons();
      icons.addAll(icon);
      if (icons.size() > 1) {
         icons.remove(0);
      }
   }

   private void updateProjectView() {
      final Project project = Model.activeWorkItem.get().getProject();
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

   public void setStage(final Stage primaryStage) {
      this.mainStage = primaryStage;
   }

   @FXML
   public void addNewProject(final ActionEvent ae) {
      LOG.info("Add new project clicked");
      // TODO somewhat duplicate dialog of create and edit
      final Dialog<Project> dialog = setUpDialogProject("Create new project", "Create a new project");

      final GridPane grid = setUpAddNewProjectGridPane("", Color.WHITE, true);

      // TODO disable OK button if no name is set
      dialog.getDialogPane().setContent(grid);

      dialogResultConverter(dialog, grid);
      mainStage.setAlwaysOnTop(false);
      final Optional<Project> result = dialog.showAndWait();
      mainStage.setAlwaysOnTop(true);

      result.ifPresent(project -> {
         controller.addNewProject(project.getName(), project.isWork(), project.getColor(), project.getIndex());
         realignProjectList();
      });
   }

}
