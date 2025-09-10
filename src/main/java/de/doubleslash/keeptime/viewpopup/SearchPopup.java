package de.doubleslash.keeptime.viewpopup;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchPopup<T> {
   private final TextField searchField = new TextField();
   private final Button showSuggestionsButton = new Button("▼");
   private final ListView<T> suggestionList = new ListView<>();
   private final Popup popup = new Popup();
   private ObservableList<T> allItems = FXCollections.observableArrayList();
   private Consumer<T> selectionHandler;
   private Function<T, String> displayTextFunction = Object::toString;

   private ObservableList<T> observedItemsForListener = null;
   private final ListChangeListener<T> listChangeListener = c -> filterList(searchField.getText());

   public SearchPopup() {
      popup.setAutoHide(true);
      popup.getContent().add(suggestionList);

      setupStyle();

      showSuggestionsButton.setOnAction(ae -> {
         show(searchField);
         searchField.requestFocus();
      });

      searchField.textProperty().addListener((obs, oldText, newText) -> filterList(newText));
      // Hide popup when focus is lost from both field and list
      ChangeListener<Boolean> hidePopupListener = (obs, was, isNow) -> {
         if (!searchField.isFocused() && !suggestionList.isFocused()) {
            popup.hide();
         }
      };
      searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
         if (isNowFocused && !suggestionList.getItems().isEmpty()) {
            show(searchField);
         }
      });
      suggestionList.focusedProperty().addListener(hidePopupListener);

      // Keyboard navigation
      searchField.setOnKeyPressed(ev -> {
         if (ev.getCode() == KeyCode.DOWN && !suggestionList.getItems().isEmpty()) {
            show(searchField);
            suggestionList.requestFocus();
            suggestionList.getSelectionModel().selectFirst();
            ev.consume();
         }
      });
      suggestionList.setOnKeyPressed(ev -> {
         if (ev.getCode() == KeyCode.ENTER) {
            T selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
               handleSelection(selected);
               popup.hide();
            }
         } else if (ev.getCode() == KeyCode.UP &&
               suggestionList.getSelectionModel().getSelectedIndex() == 0) {
            searchField.requestFocus();
         } else if (ev.getCode() == KeyCode.ESCAPE) {
            hide();
            searchField.getParent().requestFocus();
         }
      });

      // Mouse events
      suggestionList.setOnMouseClicked(ev -> {
         T selected = suggestionList.getSelectionModel().getSelectedItem();
         if (selected != null) {
            handleSelection(selected);
         }

      });

      searchField.setOnMouseClicked(ev -> show(searchField));
   }

   private void filterList(String input) {
      String filter = (input == null) ? "" : input.trim().toLowerCase();
      ObservableList<T> filtered = FXCollections.observableArrayList(
            allItems.stream()
                    .filter(item -> displayTextFunction.apply(item).toLowerCase().contains(filter))
                    .collect(Collectors.toList())
      );
      suggestionList.setItems(filtered);
      if (!filtered.isEmpty() && searchField.isFocused()) {
         show(searchField);
      } else {
         popup.hide();
      }
   }

   private void handleSelection(T selected) {
      if (selectionHandler != null) selectionHandler.accept(selected);
      popup.hide();
      searchField.clear();
      searchField.setPromptText("Select project…");
      searchField.getParent().requestFocus();
   }

   private void setupStyle() {
      HBox.setHgrow(searchField, Priority.ALWAYS);
      searchField.setPromptText("Select project…");
      searchField.getStyleClass().add("combo-box");

      suggestionList.getStyleClass().add("scroll-pane");
      suggestionList.setMaxHeight(200);

      suggestionList.setCellFactory(listView -> new ListCell<>() {
         private final Label label = new Label();
         private final StackPane pane = new StackPane(label);

         {
            label.setWrapText(true);
            label.setStyle("-fx-padding: 5;");
            pane.setAlignment(Pos.CENTER_LEFT);
            pane.setMinWidth(0);
            pane.setPrefWidth(1);
         }

         @Override
         protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
               setGraphic(null);
            } else {
               label.setText(displayTextFunction.apply(item));
               setGraphic(pane);
            }
         }
      });

      showSuggestionsButton.getStyleClass().add("secondary-button");
   }



   public void setItems(ObservableList<T> items) {
      if (observedItemsForListener != null) {
         observedItemsForListener.removeListener(listChangeListener);
      }
      this.allItems = items != null ? items : FXCollections.observableArrayList();
      allItems.addListener(listChangeListener);
      observedItemsForListener = allItems;
      filterList(searchField.getText());
   }

   public void setOnItemSelected(Consumer<T> handler) {
      this.selectionHandler = handler;
   }

   public void setDisplayTextFunction(Function<T, String> func) {
      this.displayTextFunction = func != null ? func : Object::toString;
      filterList(searchField.getText());
   }

   public TextField getTextField() {
      return searchField;
   }

   public Button getSuggestionsButton() {
      return showSuggestionsButton;
   }

   public void show(Node owner) {
      if (owner == null) return;
      Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
      suggestionList.setPrefWidth(searchField.getWidth());
      popup.show(owner, bounds.getMinX(), bounds.getMaxY());
   }

   public void hide() {
      popup.hide();
   }
}