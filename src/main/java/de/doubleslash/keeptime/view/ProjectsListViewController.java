package de.doubleslash.keeptime.view;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.FontProvider;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ProjectsListViewController {

   private static final Logger LOG = LoggerFactory.getLogger(ProjectsListViewController.class);

   private final Model model;
   private final Controller controller;
   private final Stage mainStage;
   private final Map<Project, Label> elapsedProjectTimeLabelMap = new HashMap<>();
   private final ListView<Project> availableProjectsListView;
   private final Map<Project, Node> projectSelectionNodeMap;
   private final FilteredList<Project> filteredData;

   private boolean hideable;

   public ProjectsListViewController(final Model model, final Controller controller, final Stage mainStage,
         final ListView<Project> availableProjectsListView, final TextField searchTextField, final boolean hideable) {
      this.model = model;
      this.controller = controller;
      this.hideable = hideable;
      this.mainStage = mainStage;
      this.availableProjectsListView = availableProjectsListView;
      availableProjectsListView.setCellFactory(callback -> returnListCellOfProject());

      filteredData = new FilteredList<>(model.getSortedAvailableProjects(), p -> true);
      availableProjectsListView.setItems(filteredData);

      projectSelectionNodeMap = new HashMap<>(model.getAvailableProjects().size());

      for (final Project project : model.getSortedAvailableProjects()) {
         if (project.isEnabled()) {
            final Node node = addProjectToProjectList(project);
            projectSelectionNodeMap.put(project, node);
         }
      }

      // TODO why is there no nice way for listview height?
      // https://stackoverflow.com/questions/17429508/how-do-you-get-javafx-listview-to-be-the-height-of-its-items
      final Consumer<Double> updateSize = height -> {
         if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s%f", "update size ", height));
            availableProjectsListView.setPrefHeight(height);
            mainStage.sizeToScene(); // also update scene size
         }
      };

      searchTextField.textProperty().addListener((a, b, newValue) -> {
         LOG.info("New filter value: " + newValue);
         // TODO do i always have to create a new predicate?
         filteredData.setPredicate(project -> {
            // If filter text is empty, display all persons.
            boolean returnValue = false;
            if (newValue == null || newValue.isEmpty()) {
               returnValue = true;
            }

            final String lowerCaseFilter = newValue.toLowerCase();

            if (project.getName().toLowerCase().contains(lowerCaseFilter)) {
               returnValue = true; // Filter matches first name.
            }

            return returnValue; // Does not match.
         });
         LOG.info("Projects to show '{}'.", filteredData.size());
      });

      searchTextField.setOnKeyPressed(eh -> {
         final MultipleSelectionModel<Project> selectionModel = availableProjectsListView.getSelectionModel();
         final int selectedIndex = selectionModel.getSelectedIndex();
         switch (eh.getCode()) {
         case UP:
            LOG.debug("Arrow up pressed.");
            selectionModel.select(selectedIndex - 1);
            eh.consume();
            break;
         case DOWN:
            LOG.debug("Arrow down pressed.");
            selectionModel.select(selectedIndex + 1);
            eh.consume();
            break;
         case ESCAPE:
            if (hideable) {
               searchTextField.getScene().getWindow().hide();
            }
            break;
         default:
            break;
         }
         availableProjectsListView.scrollTo(selectionModel.getSelectedIndex());
         LOG.debug("Selected list item index '{}'.", selectionModel.getSelectedIndex());
      });

      // enter pressed in textfield
      searchTextField.setOnAction(ev -> {
         final Project selectedItem = availableProjectsListView.getSelectionModel().getSelectedItem();
         if (selectedItem != null) {
            changeProject(selectedItem, 0);
         }
      });

      availableProjectsListView.getSelectionModel().selectFirst();

      model.getAvailableProjects().addListener(this::handleAvailableProjectsListChange);

      searchTextField.setPromptText("Search");
   }

   public void changeProject(final Project newProject, final long minusSeconds) {
      if (hideable) {
         mainStage.hide();
      }
      controller.changeProject(newProject, minusSeconds);
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
      // TODO changing the model is not ok from here, but the list is not resorted
      final Comparator<? super Project> comparator = model.getSortedAvailableProjects().getComparator();
      model.getSortedAvailableProjects().setComparator(null); // TODO is there a better way?
      model.getSortedAvailableProjects().setComparator(comparator);
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
         LOG.error("Could not load '{}'.", loader.getLocation());
         throw new FXMLLoaderException(e1);
      }

      final Label projectNameLabel = (Label) projectElement.getChildren().get(0);
      final Label elapsedTimeLabel = (Label) projectElement.getChildren().get(1);
      elapsedTimeLabel.textFillProperty().bind(ViewController.fontColorProperty);
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

      final MenuItem changeWithTimeMenuItem = new MenuItem("Change with time");
      changeWithTimeMenuItem.setOnAction(e -> {
         final ChangeWithTimeDialog changeWithTimeDialog = new ChangeWithTimeDialog(model,
               ViewController.activeWorkSecondsProperty, p);
         mainStage.setAlwaysOnTop(false);
         final Optional<Integer> result = changeWithTimeDialog.showAndWait();
         result.ifPresent(minusSeconds -> changeProject(p, minusSeconds));
         mainStage.setAlwaysOnTop(true);
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

            // updateProjectView(); // TODO how to update currentProjectLabel when project was edited?
            realignProjectList();
         });
      });

      // Add MenuItem to ContextMenu
      contextMenu.getItems().addAll(changeWithTimeMenuItem, editMenuItem, deleteMenuItem);

      return projectElement;
   }

   private void handleAvailableProjectsListChange(final Change<? extends Project> lis) {
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
                  changeProject(model.getIdleProject(), 0);
               }
               projectSelectionNodeMap.remove(project);
               availableProjectsListView.getItems().remove(project);
            }
         }
      }
      realignProjectList();
   }

   private Dialog<ButtonType> setUpDialogButtonType(final String title, final String headerText) {
      final Dialog<ButtonType> dialog = new Dialog<>();
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

   private void editProject(final ObservableList<Node> nodes, final Project p) {
      final TextField projectNameTextField = (TextField) nodes.get(1);
      final ColorPicker colorPicker = (ColorPicker) nodes.get(3);
      final CheckBox isWorkCheckBox = (CheckBox) nodes.get(5);
      final Spinner<Integer> indexSpinner = (Spinner<Integer>) nodes.get(7);
      controller.editProject(p, projectNameTextField.getText(), colorPicker.getValue(), isWorkCheckBox.isSelected(),
            indexSpinner.getValue());
   }

   public void tick() {
      for (final Entry<Project, Label> entry : elapsedProjectTimeLabelMap.entrySet()) {
         final Project p = entry.getKey();
         final Label label = entry.getValue();

         final long seconds = model.getPastWorkItems().stream().filter(work -> work.getProject().getId() == p.getId())
               .mapToLong(work -> Duration.between(work.getStartTime(), work.getEndTime()).getSeconds()).sum();
         label.setText(DateFormatter.secondsToHHMMSS(seconds));
      }
   }

   public ListCell<Project> returnListCellOfProject() {
      return new ListCell<Project>() {

         @Override
         protected void updateItem(final Project item, final boolean empty) {
            super.updateItem(item, empty);

            setText("");
            if (item == null || empty) {
               LOG.error("EMPTY");
               setGraphic(null);
            } else {
               LOG.trace("Item: '{}' -> '{}'", item.getName(), projectSelectionNodeMap.get(item));
               setGraphic(projectSelectionNodeMap.get(item));
            }
         }

      };
   }

}
