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

package de.doubleslash.keeptime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.doubleslash.keeptime.common.FontProvider;
import de.doubleslash.keeptime.common.OS;
import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Settings;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.view.ViewController;
import de.doubleslash.keeptime.viewpopup.GlobalScreenListener;
import de.doubleslash.keeptime.viewpopup.ViewControllerPopup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@SpringBootApplication
public class App extends Application {

   private static final Logger LOG = LoggerFactory.getLogger(App.class);

   private ConfigurableApplicationContext springContext;

   private Stage popupViewStage;

   private Model model;

   private Controller controller;

   private ViewController viewController;

   private GlobalScreenListener globalScreenListener;

   @Override
   public void init() throws Exception {
      LOG.info("Starting KeepTime.");
      final DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();
      defaultExceptionHandler.register();

      springContext = SpringApplication.run(App.class);
      ApplicationProperties applicationProperties = springContext.getBean(ApplicationProperties.class);
      LOG.info("KeepTime Version: '{}'.", applicationProperties.getBuildVersion());
      LOG.info("KeepTime Build Timestamp: '{}'.", applicationProperties.getBuildTimestamp());
      LOG.info("KeepTime Git Infos: id '{}', branch '{}', time '{}', dirty '{}'.",
            applicationProperties.getGitCommitId(), applicationProperties.getGitBranch(),
            applicationProperties.getGitCommitTime(), applicationProperties.getGitDirty());

      model = springContext.getBean(Model.class);
      controller = springContext.getBean(Controller.class);
      controller.enableAutoSave();
      model.setSpringContext(springContext);
   }

   @Override
   public void start(final Stage primaryStage) {
      LOG.info("Initialising the UI");
      try {
         initialiseApplication(primaryStage);
         LOG.info("UI successfully initialised.");
      } catch (final Exception e) {
         LOG.error("There was an error while initialising the UI", e);

         final Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("Error");
         alert.setHeaderText("Could not start application");
         alert.setContentText("Please send the error with your logs folder to a developer");

         final StringWriter sw = new StringWriter();
         final PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         final String exceptionText = sw.toString();

         final Label label = new Label("The exception stacktrace was:");

         final TextArea textArea = new TextArea(exceptionText);
         textArea.setEditable(false);
         textArea.setWrapText(true);

         textArea.setMaxWidth(Double.MAX_VALUE);
         textArea.setMaxHeight(Double.MAX_VALUE);
         GridPane.setVgrow(textArea, Priority.ALWAYS);
         GridPane.setHgrow(textArea, Priority.ALWAYS);

         final GridPane expContent = new GridPane();
         expContent.setMaxWidth(Double.MAX_VALUE);
         expContent.add(label, 0, 0);
         expContent.add(textArea, 0, 1);

         alert.getDialogPane().setExpandableContent(expContent);
         alert.showAndWait();
         System.exit(1);
      }
   }

   private void initialiseApplication(final Stage primaryStage) throws Exception {
      FontProvider.loadFonts();
      readSettings();

      final List<Work> todaysWorkItems = model.getWorkRepository().findByStartDateOrderByStartTimeAsc(LocalDate.now());

      LOG.info("Found {} past work items", todaysWorkItems.size());
      model.getPastWorkItems().addAll(todaysWorkItems);

      final List<Project> projects = model.getProjectRepository().findAll();

      LOG.debug("Found '{}' projects", projects.size());
      if (projects.isEmpty()) {
         LOG.info("Adding default project");
         projects.add(Model.DEFAULT_PROJECT);
         model.getProjectRepository().save(Model.DEFAULT_PROJECT);
      }

      model.getAllProjects().addAll(projects);
      model.getAvailableProjects()
           .addAll(model.getAllProjects().stream().filter(Project::isEnabled).collect(Collectors.toList()));

      // set default project
      final Optional<Project> findAny = projects.stream().filter(Project::isDefault).findAny();
      if (findAny.isPresent()) {
         model.setIdleProject(findAny.get());
         LOG.debug("Using project '{}' as default project.", model.getIdleProject());
      }

      primaryStage.setOnHiding(we -> {
         popupViewStage.close();
         if (globalScreenListener != null) {
            globalScreenListener.shutdown(); // deregister, as this will keep app running
         }
      });

      initialiseUI(primaryStage);
      initialisePopupUI(primaryStage);
   }

   private void readSettings() {
      LOG.debug("Reading configuration");

      final List<Settings> settingsList = model.getSettingsRepository().findAll();
      final Settings settings;
      if (settingsList.isEmpty()) {
         LOG.info("Empty settings. Set default");
         settings = new Settings();
         settings.setTaskBarColor(model.taskBarColor.get());

         settings.setDefaultBackgroundColor(Model.ORIGINAL_DEFAULT_BACKGROUND_COLOR);
         settings.setDefaultFontColor(Model.ORIGINAL_DEFAULT_FONT_COLOR);

         settings.setHoverBackgroundColor(Model.ORIGINAL_HOVER_BACKGROUND_COLOR);
         settings.setHoverFontColor(Model.ORIGINAL_HOVER_Font_COLOR);
         settings.setUseHotkey(false);
         settings.setDisplayProjectsRight(false);
         settings.setHideProjectsOnMouseExit(false);
         model.getSettingsRepository().save(settings);
      } else {
         LOG.info("Got settings from database");
         settings = settingsList.get(0);
      }

      model.defaultBackgroundColor.set(settings.getDefaultBackgroundColor());
      model.defaultFontColor.set(settings.getDefaultFontColor());
      model.hoverBackgroundColor.set(settings.getHoverBackgroundColor());
      model.hoverFontColor.set(settings.getHoverFontColor());
      model.taskBarColor.set(settings.getTaskBarColor());
      model.useHotkey.set(settings.isUseHotkey());
      model.displayProjectsRight.set(settings.isDisplayProjectsRight());
      model.hideProjectsOnMouseExit.set(settings.isHideProjectsOnMouseExit());
      model.screenSettings.proportionalX.set(settings.getWindowXProportion());
      model.screenSettings.proportionalY.set(settings.getWindowYProportion());
      model.screenSettings.screenHash.set(settings.getScreenHash());
      model.screenSettings.saveWindowPosition.set(settings.isSaveWindowPosition());
      model.remindIfNotesAreEmpty.set(settings.isRemindIfNotesAreEmpty());
      model.remindIfNotesAreEmptyOnlyForWorkEntry.set(settings.isRemindIfNotesAreEmptyOnlyForWorkEntry());
      model.confirmClose.set(settings.isConfirmClose());

   }

   private void initialisePopupUI(final Stage primaryStage) throws IOException {
      LOG.debug("Initialising popup UI.");

      popupViewStage = new Stage();
      popupViewStage.initOwner(primaryStage);
      // Load root layout from fxml file.
      final FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Resources.getResource(RESOURCE.FXML_VIEW_POPUP_LAYOUT));
      loader.setControllerFactory(springContext::getBean);
      final Parent popupLayout = loader.load();
      popupViewStage.initStyle(StageStyle.TRANSPARENT);
      // Show the scene containing the root layout.
      final Scene popupScene = new Scene(popupLayout, Color.TRANSPARENT);

      popupViewStage.setResizable(false);
      popupViewStage.setScene(popupScene);
      // Give the controller access to the main app.
      popupViewStage.setAlwaysOnTop(true);
      final ViewControllerPopup viewControllerPopupController = loader.getController();
      viewControllerPopupController.setStage(popupViewStage);

      if (OS.isWindows()) {
         globalScreenListener = new GlobalScreenListener();
         globalScreenListener.register(model.useHotkey.get());
         globalScreenListener.setViewController(viewControllerPopupController);
         model.useHotkey.addListener((a, b, newValue) -> globalScreenListener.register(newValue));
      }
   }

   private void initialiseUI(final Stage primaryStage) throws IOException {
      LOG.debug("Initialising main UI.");

      // Load root layout from fxml file.
      final FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Resources.getResource(RESOURCE.FXML_VIEW_LAYOUT));
      loader.setControllerFactory(springContext::getBean);
      final Pane mainPane = loader.load();
      primaryStage.initStyle(StageStyle.TRANSPARENT);
      primaryStage.getIcons().add(new Image(Resources.getResource(RESOURCE.ICON_MAIN).toString()));
      // Show the scene containing the root layout.
      final Scene mainScene = new Scene(mainPane, Color.TRANSPARENT);

      registerMinimizeEventlistener(mainScene, primaryStage);
      registerMaximizeEventlistener(mainScene, primaryStage);

      primaryStage.setTitle("KeepTime");
      primaryStage.setScene(mainScene);
      primaryStage.setAlwaysOnTop(true);
      primaryStage.setResizable(false);

      primaryStage.setOnCloseRequest(windowEvent -> LOG.info("On close request"));

      primaryStage.show();
      viewController = loader.getController();
      // Give the controller access to the main app.
      viewController.setStage(primaryStage);

   }

   private void registerMinimizeEventlistener(final Scene mainScene, final Stage primaryStage) {
      final KeyCombination keyComb = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.META_DOWN);
      mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
         if (keyComb.match(keyEvent)) {
            LOG.info("KeyCombination '{}' was pressed: Minimizing window.", keyComb);
            primaryStage.setIconified(true);
            keyEvent.consume();
         }
      });
   }

   private void registerMaximizeEventlistener(final Scene mainScene, final Stage primaryStage) {
      final KeyCombination keyComb = new KeyCodeCombination(KeyCode.UP, KeyCombination.META_DOWN);
      mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
         if (keyComb.match(keyEvent)) {
            LOG.info("KeyCombination  '{}' was pressed: Maximizing window.", keyComb);
            primaryStage.setIconified(false);
            keyEvent.consume();
         }
      });
   }

   @Override
   public void stop() throws Exception {
      springContext.stop();
   }

}
