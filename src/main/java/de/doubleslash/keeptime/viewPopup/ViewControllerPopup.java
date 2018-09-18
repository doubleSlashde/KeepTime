package de.doubleslash.keeptime.viewPopup;

import java.awt.Point;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ViewControllerPopup {

   private static final int LIST_CELL_HEIGHT = 23 + 2;

   private final Logger LOG = LoggerFactory.getLogger(this.getClass());

   @FXML
   private Pane pane;

   @FXML
   private TextField searchTextField;
   @FXML
   private ListView<Project> projectListView;

   private Stage stage;

   private Scene scene;

   private Controller controller;
   private Model model;

   private FilteredList<Project> filteredData;

   private void changeProject(final Project item) {
      LOG.info("Change project to '" + item.getName() + "'.");

      // ask for a note for the current project
      final TextInputDialog dialog = new TextInputDialog(model.activeWorkItem.get().getNotes());
      dialog.setTitle("Note for current project");
      dialog.setHeaderText(
            "Add a note for '" + model.activeWorkItem.get().getProject().getName() + "' before changing project?");
      dialog.setContentText("Note: ");

      this.stage.setAlwaysOnTop(false);
      final Optional<String> result = dialog.showAndWait();
      this.stage.setAlwaysOnTop(true);

      result.ifPresent(note -> {
         controller.setComment(note);
         hide();
         controller.changeProject(item);
      });
   }

   @FXML
   private void initialize() throws IOException {

      // TODO the cells do not refresh, if a project was changed
      projectListView.setCellFactory(new Callback<ListView<Project>, ListCell<Project>>() {
         @Override
         public ListCell<Project> call(final ListView<Project> list) {
            return new ListCell<Project>() {
               @Override
               protected void updateItem(final Project project, final boolean empty) {
                  super.updateItem(project, empty);

                  if (project == null || empty) {
                     setText(null);
                     // setPrefHeight(0);
                  } else {
                     setOnMouseClicked((ev) -> {
                        changeProject(project);
                     });
                     // setPrefHeight(24);
                     final boolean isActiveProject = project == model.activeWorkItem.get().getProject();
                     setText((isActiveProject ? "* " : "") + project.getName());
                     setTextFill(project.getColor());
                     setUnderline(project.isWork());
                  }
               }

            };
         }
      });

      // TODO why is there no nice way for listview height?
      // https://stackoverflow.com/questions/17429508/how-do-you-get-javafx-listview-to-be-the-height-of-its-items
      final Consumer<Double> updateSize = (height) -> {
         LOG.debug("update size" + height);
         projectListView.setPrefHeight(height);
         stage.sizeToScene(); // also update scene size
      };

      searchTextField.textProperty().addListener((a, b, newValue) -> {
         // TODO do i always have to create a new predicate?
         filteredData.setPredicate(project -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
               return true;
            }

            final String lowerCaseFilter = newValue.toLowerCase();

            if (project.getName().toLowerCase().contains(lowerCaseFilter)) {
               return true; // Filter matches first name.
            }

            return false; // Does not match.
         });

         projectListView.getSelectionModel().selectFirst();
         updateSize.accept((double) (filteredData.size() * LIST_CELL_HEIGHT));
      });

      searchTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
         @Override
         public void handle(final KeyEvent event) {
            final MultipleSelectionModel<Project> selectionModel = projectListView.getSelectionModel();
            final int selectedIndex = selectionModel.getSelectedIndex();
            switch (event.getCode()) {
            case UP:
               selectionModel.select(selectedIndex - 1);
               event.consume();
               break;
            case DOWN:
               selectionModel.select(selectedIndex + 1);
               event.consume();
               break;
            case ESCAPE:
               hide();
               break;
            default:
               break;

            }
         }
      });
      // enter pressed in textfield
      searchTextField.setOnAction((ev) -> {
         final Project selectedItem = projectListView.getSelectionModel().getSelectedItem();
         if (selectedItem != null) {
            changeProject(selectedItem);
         }
      });

      projectListView.getSelectionModel().selectFirst();
      projectListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

   }

   public void setController(final Controller controller, final Model model) {
      this.controller = controller;
      this.model = model;

      filteredData = new FilteredList<>(model.sortedAvailableProjects, p -> true);
      projectListView.setItems(filteredData);
   }

   public void setStage(final Stage primaryStage, final Scene scene) {
      this.stage = primaryStage;
      this.scene = scene;

      // if we loose focus, hide the stage
      stage.focusedProperty().addListener((a, b, newValue) -> {
         if (!newValue) {
            hide();
         }
      });
   }

   public void show(final Point mouseLocation) {
      if (!stage.isShowing()) {
         LOG.info("Showing popup");
         projectListView.getSelectionModel().select(0);
         projectListView.refresh();

         searchTextField.setText("a"); // trigger to update list size
         searchTextField.setText("");

         stage.setX(mouseLocation.getX() - 2);
         stage.setY(mouseLocation.getY() - 2);
         stage.show();
         stage.requestFocus();
         searchTextField.requestFocus();
      }
   }

   private void hide() {
      if (stage.isShowing()) {

         searchTextField.setText("");

         stage.hide();
      }
   }

}
