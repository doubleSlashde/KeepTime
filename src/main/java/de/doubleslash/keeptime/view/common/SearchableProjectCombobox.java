package de.doubleslash.keeptime.view.common;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import de.doubleslash.keeptime.common.ColorHelper;
import de.doubleslash.keeptime.common.DoesProjectMatchSearchFilterPredicate;
import de.doubleslash.keeptime.common.StyleUtils;
import de.doubleslash.keeptime.model.Project;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class SearchableProjectCombobox extends ComboBox<Project> {

   private boolean comboChange;
   private Project selectedProject;
   private Color fontColor;
   FilteredList<Project> filteredList;

   public SearchableProjectCombobox() {
      super();

      final SearchableProjectCombobox comboBox = this;

      // strg+a Behaviour bug hack
      // https://stackoverflow.com/questions/51943654/javafx-combobox-make-control-a-select-all-in-text-box-while-dropdown-is-visi
      this.setOnShown(e -> {
         final ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) comboBox.getSkin();
         final ListView<?> list = (ListView<?>) skin.getPopupContent();
         final KeyCodeCombination ctrlA = new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN);
         list.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (ctrlA.match(keyEvent)) {
               this.getEditor().selectAll();
            }
         });
         this.setOnShown(null);
      });

      // color Dropdown Options
      this.setCellFactory(listView -> new ListCell<Project>() {
         @Override
         protected void updateItem(final Project project, final boolean empty) {
            super.updateItem(project, empty);
            if (project == null || empty) {
               setGraphic(null);
            } else {

               setTextFill(project.getColor());
               setText(project.getName());

               setUnderline(project.isWork());
            }
         }
      });

      // set text of selected value
      this.setConverter(new StringConverter<Project>() {
         @Override
         public String toString(final Project project) {
            if (project == null) {
               return null;
            } else {
               return project.getName();
            }
         }

         @Override
         public Project fromString(final String string) {
            // ignores String and gets selected Value of ComboBox
            return comboBox.getValue();
         }
      });

      this.valueProperty().addListener(
            (final ObservableValue<? extends Project> observable, final Project oldValue, final Project newValue) -> {
               if (newValue == null) {
                  return;
               }

               selectedProject = newValue;
               comboChange = true;
               // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
               Platform.runLater(() -> {
                  setTextColor(this.getEditor(), newValue.getColor());
               });
            }

      );

      this.getEditor().textProperty().addListener(new ChangeListener<String>() {

         boolean isValidProject = true;

         @Override
         public void changed(final ObservableValue<? extends String> observable, final String oldValue,
               final String newValue) {

            // ignore selectionChanges
            if (comboChange) {
               comboChange = false;
               isValidProject = true;
               return;
            }

            // is necessary to not autoselect same Project if Project was selected
            if (isValidProject) {
               isValidProject = false;
               comboBox.getSelectionModel().clearSelection();
               // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
               Platform.runLater(() -> {
                  setTextColor(comboBox.getEditor(), fontColor);
               });
            }

            // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
            Platform.runLater(() -> {
               comboBox.hide();

               final String searchText = comboBox.getEditor().getText();
               filteredList.setPredicate(new DoesProjectMatchSearchFilterPredicate(searchText));

               if (comboBox.getEditor().focusedProperty().get() && !isValidProject) {
                  comboBox.show();
               }

            });

         }
      });

      // manages Focusbehaviour
      this.getEditor().focusedProperty().addListener((final ObservableValue<? extends Boolean> observable,
            final Boolean oldIsFocused, final Boolean newIsFocused) -> {
         if (newIsFocused) {
            // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
            Platform.runLater(() -> comboBox.getEditor().selectAll());
         } else {
            // needed to avoid exception on empty textfield https://bugs.openjdk.java.net/browse/JDK-8081700
            Platform.runLater(() -> comboBox.hide());
         }

      });

      // on
      this.setOnKeyReleased(ke -> {
         if (ke.getCode() == KeyCode.ENTER && this.getSelectionModel().isEmpty()) {
            if (!this.getItems().isEmpty()) {
               this.getSelectionModel().selectFirst();
               comboChange = true;
            }
         }

      });

      Platform.runLater(() -> this.requestFocus());
   }

   public Project getSelectedValue() {
      return selectedProject;

   }

   private void setColor(final Node object, final Color color) {
      final String style = StyleUtils.changeStyleAttribute(object.getStyle(), "fx-background-color",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      object.setStyle(style);
   }

   private void setTextColor(final Node object, final Color color) {
      final String style = StyleUtils.changeStyleAttribute(object.getStyle(), "fx-text-fill",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      object.setStyle(style);
   }

   public void setProject(final Project project, final Color background, final Color font) {

      this.fontColor = font;
      selectedProject = project;
      setColor(this, background);
      setColor(this.getEditor(), background);

      setTextColor(this.getEditor(), project.getColor());

      this.getSelectionModel().select(project);
   }

   public void setProjects(final ObservableList<Project> projects) {
      filteredList = new FilteredList<>(projects);
      super.setItems(filteredList);
   }
}
