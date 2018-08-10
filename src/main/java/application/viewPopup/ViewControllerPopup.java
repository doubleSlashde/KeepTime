package application.viewPopup;

import java.awt.Point;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import application.controller.Controller;
import application.model.Model;
import application.model.Project;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
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

   private static final int LIST_CELL_HEIGHT = 24 + 2;

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   @FXML
   private Pane pane;

   @FXML
   private TextField searchTextField;
   @FXML
   private ListView<Project> projectListView;
   FilteredList<Project> filteredData;

   private void changeProject(final Project item) {
      Log.info("Change project to '" + item.getName() + "'.");

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
      });

      hide();
      controller.changeProject(item);
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

      searchTextField.textProperty().addListener((a, b, newValue) -> {
         filteredData.setPredicate(project -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
               return true;
            }

            // Compare first name and last name of every person with filter
            // text.
            final String lowerCaseFilter = newValue.toLowerCase();

            if (project.getName().toLowerCase().contains(lowerCaseFilter)) {
               return true; // Filter matches first name.
            }

            return false; // Does not match.
         });
         projectListView.getSelectionModel().selectFirst();
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

   Stage stage;

   private Scene scene;

   Controller controller;
   Model model;

   public void setController(final Controller controller, final Model model) {
      this.controller = controller;
      this.model = model;

      filteredData = new FilteredList<>(model.availableProjects, p -> true);
      projectListView.setItems(filteredData);

      // TODO why is there no nice way for listview height?
      // https://stackoverflow.com/questions/17429508/how-do-you-get-javafx-listview-to-be-the-height-of-its-items
      final Consumer<Double> updateSize = (height) -> {
         projectListView.setPrefHeight(height);
         stage.sizeToScene(); // also update scene size
      };
      final IntegerBinding size = Bindings.size(filteredData);
      final IntegerBinding multiply = size.multiply(LIST_CELL_HEIGHT);
      multiply.addListener((observable, oldValue, newValue) -> {
         // TODO why is this not updated if the user added a new project
         updateSize.accept(newValue.doubleValue());
      });
      updateSize.accept((double) (filteredData.size() * LIST_CELL_HEIGHT));
   }

   public void setStage(final Stage primaryStage, final Scene scene) {
      this.stage = primaryStage;
      this.scene = scene;

      // if we loose focus, hide the final stage
      stage.focusedProperty().addListener((a, b, c) -> {
         if (!c) {
            hide();
         }
      });
   }

   public void show(final Point mouseLocation) {
      if (!stage.isShowing()) {
         Log.info("Showing popup");
         projectListView.getSelectionModel().select(0);
         projectListView.refresh();

         stage.setX(mouseLocation.getX() - 2);
         stage.setY(mouseLocation.getY() - 2);
         stage.show();
         stage.requestFocus();
         searchTextField.requestFocus();

         // simulate a mouse click on title bar of window
         // Robot robot;
         // try {
         // robot = new Robot();
         // robot.mouseMove((int) mouseLocation.getX(), (int) mouseLocation.getY());
         // robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
         // robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
         // } catch (final AWTException e) {
         // // XXX Auto-generated catch block
         // e.printStackTrace();
         // }

         // final Alert alert = new Alert(AlertType.CONFIRMATION);
         // alert.initModality(Modality.APPLICATION_MODAL);
         // alert.show();
         // TODO the window wont get active -> no focus
         Platform.runLater(() -> {
            // stage.requestFocus();
            // stage.setAlwaysOnTop(false);
            // stage.setAlwaysOnTop(true);
            // stage.setAlwaysOnTop(false);
            // stage.toFront();
            // stage.setAlwaysOnTop(true);
            // stage.setIconified(true);
            // Platform.runLater(() -> {
            // stage.setIconified(false);
            // });
            // final Stage stage2 = new Stage();
            // stage2.setTitle("666");
            // stage2.show();
         });
      }
   }

   private void hide() {
      if (stage.isShowing()) {

         searchTextField.setText("");

         stage.hide();
      }
   }

}
