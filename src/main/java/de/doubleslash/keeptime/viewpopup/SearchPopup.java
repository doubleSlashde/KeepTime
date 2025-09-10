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

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchPopup<T> {
   private final TextField searchField = new TextField();
   private final Button showSuggestionsButton = new Button("▼");
   private final ListView<T> suggestionList = new ListView<>();
   private final Popup popup = new Popup();
   private final HBox container;

   private ObservableList<T> allItems = FXCollections.observableArrayList();
   private ObservableList<T> observedItemsForListener = null;
   private final ListChangeListener<T> listChangeListener = c -> filterList(searchField.getText());

   private Function<T, String> displayTextFunction = Object::toString;
   private String promptText = "Select item…";
   private double maxSuggestionHeight = 200;

   private BiConsumer<T, SearchPopup<T>> onItemSelected = (item, popup) -> {};
   private boolean clearFieldAfterSelection = false;

   public SearchPopup() {
      this(FXCollections.observableArrayList());
   }

   public SearchPopup(ObservableList<T> items) {
      container = new HBox(searchField, showSuggestionsButton);
      container.getStyleClass().add("search-popup-container");
      container.setAlignment(Pos.CENTER_LEFT);
      HBox.setHgrow(searchField, Priority.ALWAYS);

      setItems(items);

      setupUI();
      setupListeners();
   }

   private void setupUI() {
      popup.setAutoHide(true);
      popup.getContent().add(suggestionList);

      searchField.setPromptText(promptText);
      searchField.getStyleClass().add("search-popup");
      searchField.setMaxWidth(Double.MAX_VALUE);

      showSuggestionsButton.getStyleClass().add("search-popup-button");

      suggestionList.setMaxHeight(maxSuggestionHeight);
      suggestionList.getStyleClass().add("scroll-pane");

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
   }

   private void setupListeners() {
      showSuggestionsButton.setOnAction(ae -> {
         show(searchField);
         searchField.requestFocus();
      });

      ChangeListener<Boolean> hidePopupListener = (obs, was, isNow) -> {
         if (!searchField.isFocused() && !suggestionList.isFocused()) popup.hide();
      };

      searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
         if (isNowFocused && !clearFieldAfterSelection) {
            filterList(""); // Show all items
            show(searchField);
            searchField.selectAll(); // <--- This line selects all text!
         }
      });

      searchField.setOnKeyPressed(ev -> {
         if (ev.getCode() == KeyCode.DOWN && !suggestionList.getItems().isEmpty()) {
            show(searchField);
            suggestionList.requestFocus();
            suggestionList.getSelectionModel().selectFirst();
            ev.consume();
         }
      });

      searchField.setOnMouseClicked(ev -> {
         if (!clearFieldAfterSelection) {
            filterList(""); // Show all items
            show(searchField);
            searchField.selectAll();
         } else {
            filterList(searchField.getText());
            show(searchField);
         }
      });

      suggestionList.focusedProperty().addListener(hidePopupListener);

      suggestionList.setOnKeyPressed(ev -> {
         if (ev.getCode() == KeyCode.ENTER) {
            T selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null) handleSelection(selected);
         } else if (ev.getCode() == KeyCode.UP && suggestionList.getSelectionModel().getSelectedIndex() == 0) {
            searchField.requestFocus();
         } else if (ev.getCode() == KeyCode.ESCAPE) {
            hide();
            container.requestFocus();
         }
      });

      suggestionList.setOnMouseClicked(ev -> {
         T selected = suggestionList.getSelectionModel().getSelectedItem();
         if (selected != null) handleSelection(selected);
      });

      searchField.textProperty().addListener((obs, oldText, newText) -> {
         filterList(newText);
      });
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
      if (clearFieldAfterSelection) {
         clear();
      } else {
         searchField.setText(selected == null ? "" : displayTextFunction.apply(selected));
      }
      onItemSelected.accept(selected, this);
      popup.hide();
      container.requestFocus();
   }

   public void setItems(ObservableList<T> items) {
      if (observedItemsForListener != null)
         observedItemsForListener.removeListener(listChangeListener);
      this.allItems = items != null ? items : FXCollections.observableArrayList();
      allItems.addListener(listChangeListener);
      observedItemsForListener = allItems;
      filterList(searchField.getText());
   }

   public void setDisplayTextFunction(Function<T, String> func) {
      this.displayTextFunction = func != null ? func : Object::toString;
      filterList(searchField.getText());
   }

   public void setPromptText(String text) {
      this.promptText = text;
      searchField.setPromptText(text);
   }

   public void setMaxSuggestionHeight(double height) {
      this.maxSuggestionHeight = height;
      suggestionList.setMaxHeight(height);
   }

   public HBox getComboBox() {
      return container;
   }

   public void show(Node owner) {
      if (owner == null || suggestionList.getItems().isEmpty()) return;
      Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
      suggestionList.setPrefWidth(searchField.getWidth());
      popup.show(owner, bounds.getMinX(), bounds.getMaxY());
   }

   public void hide() {
      popup.hide();
   }

   public void setSelectedItem(T item) {
      if (!clearFieldAfterSelection) {
         String text = (item == null) ? "" : displayTextFunction.apply(item);
         searchField.setText(text);
      }
   }

   public T getSelectedItem() {
      String text = searchField.getText();
      for (T item : allItems) {
         if (displayTextFunction.apply(item).equals(text)) return item;
      }
      return null;
   }

   public TextField getSearchField() {
      return searchField;
   }

   public ListView<T> getSuggestionList() {
      return suggestionList;
   }

   public Button getShowSuggestionsButton() {
      return showSuggestionsButton;
   }

   public Function<T, String> getDisplayTextFunction() {
      return displayTextFunction;
   }

   public void setOnItemSelected(BiConsumer<T, SearchPopup<T>> handler) {
      this.onItemSelected = handler != null ? handler : (item, popup) -> {};
   }

   public void setClearFieldAfterSelection(boolean c) {
      this.clearFieldAfterSelection = c;
   }

   public void clear() {
      searchField.clear();
      if (!promptText.isEmpty())
         searchField.setPromptText(promptText);
   }
}