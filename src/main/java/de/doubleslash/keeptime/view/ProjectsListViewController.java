package de.doubleslash.keeptime.view;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.exceptions.FXMLLoaderException;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ProjectsListViewController {

   private static final String TOOLTIP_FORMAT_STRING = "%s%n%s";

   private static final int PROJECT_NAME_LABEL_INDEX = 0;

   private static final Logger LOG = LoggerFactory.getLogger(ProjectsListViewController.class);

   private final Model model;
   private final Controller controller;
   private final Stage mainStage;
   private final Map<Project, Label> elapsedProjectTimeLabelMap = new HashMap<>();
   private final Map<Project, Node> projectSelectionNodeMap;
   private final FilteredList<Project> filteredData;

   private boolean hideable;
   private ManageProjectController manageProjectController;

   public ProjectsListViewController(final Model model, final Controller controller, final Stage mainStage,
         final ListView<Project> availableProjectsListView, final TextField searchTextField, final boolean hideable) {
      this.model = model;
      this.controller = controller;
      this.hideable = hideable;
      this.mainStage = mainStage;
      availableProjectsListView.setCellFactory(listView -> returnListCellOfProject());

      filteredData = new FilteredList<>(model.getSortedAvailableProjects(), p -> true);
      availableProjectsListView.setItems(filteredData);

      projectSelectionNodeMap = new HashMap<>(model.getAvailableProjects().size());

      searchTextField.textProperty().addListener((a, b, newValue) -> {
         LOG.info("New filter value: " + newValue);
         // TODO do i always have to create a new predicate?
         filteredData.setPredicate(project -> {
            // If filter text is empty, display all data.
            if (newValue == null || newValue.isEmpty()) {
               return true;
            }

            final String lowerCaseFilter = newValue.toLowerCase();

            if (project.getName().toLowerCase().contains(lowerCaseFilter)
                  || project.getDescription().toLowerCase().contains(lowerCaseFilter)) {
               return true;
            }

            return false;
         });
         LOG.debug("Amount of projects to show '{}'.", filteredData.size());
         availableProjectsListView.getSelectionModel().selectFirst();
         availableProjectsListView.scrollTo(0);
      });

      searchTextField.setOnKeyPressed(eh -> {
         final MultipleSelectionModel<Project> selectionModel = availableProjectsListView.getSelectionModel();
         switch (eh.getCode()) {
         case UP:
            LOG.debug("Arrow up pressed.");
            selectionModel.selectPrevious();
            eh.consume();
            break;
         case DOWN:
            LOG.debug("Arrow down pressed.");
            selectionModel.selectNext();
            eh.consume();
            break;
         case ESCAPE:
            LOG.debug("Esc pressed.");
            if (hideable) {
               mainStage.hide();
            }
            eh.consume();
            break;
         default:
            break;
         }
         availableProjectsListView.scrollTo(selectionModel.getSelectedIndex());
      });

      // enter pressed in textfield
      searchTextField.setOnAction(ev -> {
         final Project selectedItem = availableProjectsListView.getSelectionModel().getSelectedItem();
         if (selectedItem != null) {
            changeProject(selectedItem, 0);
            searchTextField.setText("");
         }
      });

      availableProjectsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
      availableProjectsListView.getSelectionModel().selectFirst();
   }

   /**
    * Update UI
    */
   public void tick() {
      for (final Entry<Project, Label> entry : elapsedProjectTimeLabelMap.entrySet()) {
         final Project p = entry.getKey();
         final Label label = entry.getValue();

         final long seconds = model.getPastWorkItems().stream().filter(work -> work.getProject().getId() == p.getId())
               .mapToLong(work -> Duration.between(work.getStartTime(), work.getEndTime()).getSeconds()).sum();
         label.setText(DateFormatter.secondsToHHMMSS(seconds));
      }
   }

   private void changeProject(final Project newProject, final long minusSeconds) {
      if (hideable) {
         mainStage.hide();
      }
      controller.changeProject(newProject, minusSeconds);
   }

   private void addProjectToProjectSelectionNodeMap(final Project project) {
      final Node projectElement = createListEntryForProject(project);
      projectSelectionNodeMap.put(project, projectElement);
      updateTooltip(project);
   }

   private void realignProjectList() {
      LOG.debug("Sorting project view");
      // TODO changing the model is not ok from here, but the list is not resorted
      final Comparator<? super Project> comparator = model.getSortedAvailableProjects().getComparator();
      model.getSortedAvailableProjects().setComparator(null); // TODO is there a better way?
      model.getSortedAvailableProjects().setComparator(comparator);
   }

   private Node createListEntryForProject(final Project p) {
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
            editProject(p, manageProjectController);

            projectNameLabel.setText(p.getName());
            projectNameLabel.setTextFill(new Color(p.getColor().getRed() * dimFactor,
                  p.getColor().getGreen() * dimFactor, p.getColor().getBlue() * dimFactor, 1));
            projectNameLabel.setUnderline(p.isWork());
            projectNameLabel.getTooltip().setText(p.getName());

            // TODO how to update currentProjectLabel when active project was edited?
            realignProjectList();
            updateTooltip(p);
         });
      });

      // Add MenuItem to ContextMenu
      contextMenu.getItems().addAll(changeWithTimeMenuItem, editMenuItem, deleteMenuItem);

      return projectElement;
   }

   private void updateTooltip(final Project p) {
      final Pane pane = (Pane) projectSelectionNodeMap.get(p);
      final Label projectNameLabel = (Label) pane.getChildren().get(PROJECT_NAME_LABEL_INDEX);
      final String tooltipText = String.format(TOOLTIP_FORMAT_STRING, p.getName(), p.getDescription());
      final Tooltip projectTooltip = projectNameLabel.getTooltip();
      if (projectTooltip == null) {
         projectNameLabel.setTooltip(new Tooltip());
      }
      projectNameLabel.getTooltip().setText(tooltipText);
   }

   private Dialog<ButtonType> setUpDialogButtonType(final String title, final String headerText) {
      final Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle(title);
      dialog.setHeaderText(headerText);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      return dialog;
   }

   private GridPane setUpEditProjectGridPane(final Project p) {
      GridPane grid;
      final FXMLLoader loader = new FXMLLoader(Resources.getResource(RESOURCE.FXML_MANAGE_PROJECT));
      try {
         grid = loader.load();
      } catch (final IOException e) {
         throw new FXMLLoaderException("Error while loading '" + Resources.RESOURCE.FXML_MANAGE_PROJECT + "'.", e);
      }
      manageProjectController = loader.getController();
      manageProjectController.setModel(model);
      manageProjectController.secondInitialize();
      manageProjectController.setValues(p.getName(), p.getDescription(), p.getColor(), p.isWork(), p.getIndex());

      return grid;
   }

   private void editProject(final Project p, final ManageProjectController manageProjectController) {
      final TextField nameTextField = manageProjectController.getNameTextField();
      final TextArea descriptionTextArea = manageProjectController.getDescriptionTextArea();
      final ColorPicker textFillColorPicker = manageProjectController.getTextFillColorPicker();
      final CheckBox isWorkCheckBox = manageProjectController.getIsWorkCheckBox();
      final Spinner<Integer> sortIndexSpinner = manageProjectController.getSortIndexSpinner();
      controller.editProject(p, nameTextField.getText(), descriptionTextArea.getText(), textFillColorPicker.getValue(),
            isWorkCheckBox.isSelected(), sortIndexSpinner.getValue());
   }

   private ListCell<Project> returnListCellOfProject() {
      return new ListCell<Project>() {

         @Override
         protected void updateItem(final Project item, final boolean empty) {
            super.updateItem(item, empty);

            setText("");
            if (item == null || empty) {
               setGraphic(null);
            } else {
               Node graphic = projectSelectionNodeMap.get(item);
               if (graphic == null) {
                  final Project addedProject = item;
                  addProjectToProjectSelectionNodeMap(addedProject);
                  graphic = projectSelectionNodeMap.get(item);
               }
               LOG.trace("Item: '{}' -> '{}'", item.getName(), graphic);
               setGraphic(graphic);
            }
         }

      };
   }

}
