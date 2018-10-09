package de.doubleslash.keeptime.viewpopup;

import java.awt.Point;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ViewControllerPopup {

   private static final int LIST_CELL_HEIGHT = 23 + 2;

   private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

   @FXML
   private Pane pane;

   @FXML
   private TextField searchTextField;
   @FXML
   private ListView<Project> projectListView;

   private Stage stage;

   private Controller controller;

   private FilteredList<Project> filteredData;

   private void changeProject(final Project item) {
      final String info = String.format("Change project to '{%s}'.", item.getName());
      log.info(info);

      // ask for a note for the current project
      final TextInputDialog dialog = new TextInputDialog(Model.activeWorkItem.get().getNotes());
      dialog.setTitle("Note for current project");
      dialog.setHeaderText(
            "Add a note for '" + Model.activeWorkItem.get().getProject().getName() + "' before changing project?");
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
   private void initialize() {

      // TODO the cells do not refresh, if a project was changed
      projectListView.setCellFactory(cb -> returnListCellOfProject());

      // TODO why is there no nice way for listview height?
      // https://stackoverflow.com/questions/17429508/how-do-you-get-javafx-listview-to-be-the-height-of-its-items
      final Consumer<Double> updateSize = height -> {
         if (log.isDebugEnabled()) {
            log.debug(String.format("%s%f", "update size ", height));
            projectListView.setPrefHeight(height);
            stage.sizeToScene(); // also update scene size
         }
      };

      searchTextField.textProperty().addListener((a, b, newValue) -> {
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

         projectListView.getSelectionModel().selectFirst();
         updateSize.accept((double) (filteredData.size() * LIST_CELL_HEIGHT));
      });

      searchTextField.setOnKeyPressed(eh -> {
         final MultipleSelectionModel<Project> selectionModel = projectListView.getSelectionModel();
         final int selectedIndex = selectionModel.getSelectedIndex();
         switch (eh.getCode()) {
         case UP:
            selectionModel.select(selectedIndex - 1);
            eh.consume();
            break;
         case DOWN:
            selectionModel.select(selectedIndex + 1);
            eh.consume();
            break;
         case ESCAPE:
            hide();
            break;
         default:
            break;

         }
      });
      // enter pressed in textfield
      searchTextField.setOnAction(ev -> {
         final Project selectedItem = projectListView.getSelectionModel().getSelectedItem();
         if (selectedItem != null) {
            changeProject(selectedItem);
         }
      });

      projectListView.getSelectionModel().selectFirst();
      projectListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

   }

   public ListCell<Project> returnListCellOfProject() {

      return new ListCell<Project>() {

         @Override
         protected void updateItem(final Project project, final boolean empty) {
            super.updateItem(project, empty);

            if (project == null || empty) {
               setText(null);

            } else {
               setOnMouseClicked(ev -> changeProject(project));

               final boolean isActiveProject = project == Model.activeWorkItem.get().getProject();
               setText((isActiveProject ? "* " : "") + project.getName());
               setTextFill(project.getColor());
               setUnderline(project.isWork());
            }
         }

      };

   }

   public void setControllerAndModel(final Controller controller, final Model model) {
      this.controller = controller;

      filteredData = new FilteredList<>(model.getSortedAvailableProjects(), p -> true);
      projectListView.setItems(filteredData);
   }

   public void setStage(final Stage primaryStage) {
      this.stage = primaryStage;

      // if we loose focus, hide the stage
      stage.focusedProperty().addListener((a, b, newValue) -> {
         if (!newValue) {
            hide();
         }
      });
   }

   public void show(final Point mouseLocation) {
      if (!stage.isShowing()) {
         log.info("Showing popup");
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
