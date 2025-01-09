// Copyright 2019 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.viewpopup;

import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.common.ColorHelper;
import de.doubleslash.keeptime.common.StyleUtils;
import de.doubleslash.keeptime.common.time.Interval;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.view.ProjectsListViewController;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@Component
public class ViewControllerPopup {

   private static final Logger LOG = LoggerFactory.getLogger(ViewControllerPopup.class);

   @FXML
   private VBox root;

   @FXML
   private TextField searchTextField;

   @FXML
   private ListView<Project> projectListView;

   private Stage stage;

   private final Controller controller;

   private final Model model;

   private ProjectsListViewController projectsListViewController;

   public ViewControllerPopup(final Model model, final Controller controller) {
      this.model = model;
      this.controller = controller;
   }

   public void setStage(final Stage primaryStage) {
      this.stage = primaryStage;

      // if we loose focus, hide the stage
      stage.focusedProperty().addListener((a, b, newValue) -> {
         if (!newValue) {
            hide();
         }
      });

      projectsListViewController = new ProjectsListViewController(model, controller, stage, projectListView,
            searchTextField, true);

      new Interval(1).registerCallBack(() -> projectsListViewController.tick());

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

   @FXML
   private void initialize() {

      projectListView.getSelectionModel().selectFirst();

      searchTextField.setOnKeyReleased(keyEvent -> {
         if (keyEvent.getCode() == KeyCode.ESCAPE) {
            hide();
         }
      });

      projectListView.setFixedCellSize(13);

      final Runnable updateMainBackgroundColor = this::runUpdateMainBackgroundColor;
      model.hoverBackgroundColor.addListener((a, b, c) -> updateMainBackgroundColor.run());

      updateMainBackgroundColor.run();

   }

   private void hide() {
      if (stage.isShowing()) {

         searchTextField.setText("");

         stage.hide();
      }
   }

   private void runUpdateMainBackgroundColor() {
      final Color color = model.hoverBackgroundColor.get();
      final double opacity = .3;
      String style = StyleUtils.changeStyleAttribute(root.getStyle(), "fx-background-color",
            "rgba(" + ColorHelper.colorToCssRgba(color) + ")");
      style = StyleUtils.changeStyleAttribute(style, "fx-border-color",
            "rgba(" + ColorHelper.colorToCssRgb(color) + ", " + opacity + ")");
      root.setStyle(style);
   }

}
