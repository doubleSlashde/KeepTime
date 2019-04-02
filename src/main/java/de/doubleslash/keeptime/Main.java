// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

@SpringBootApplication
public class Main extends Application {

   private static final Logger LOG = LoggerFactory.getLogger(Main.class);

   public static final String VERSION = "v1.0.1b";

   private ConfigurableApplicationContext springContext;

   private Stage popupViewStage;

   private Model model;
   private Controller controller;

   private ViewController viewController;

   private GlobalScreenListener globalScreenListener;

   @Override
   public void init() throws Exception {
      LOG.info("Starting KeepTime {}", VERSION);
      final DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();
      defaultExceptionHandler.register();

      springContext = SpringApplication.run(Main.class);
      // TODO test if everywhere is used the same model
      model = springContext.getBean(Model.class);
      controller = springContext.getBean(Controller.class);
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

   private void loadFonts() {
      final List<RESOURCE> fontResources = Arrays.asList(RESOURCE.FONT_BOLD, RESOURCE.FONT_SEMI_BOLD,
            RESOURCE.FONT_REGULAR);
      LOG.info("Loading fonts '{}'", fontResources);

      for (final RESOURCE fontResource : fontResources) {
         LOG.info("Loading font '{}'", fontResource);
         final Font font = Font.loadFont(Resources.getResource(fontResource).toExternalForm(), 12);
         LOG.info("Font with name '{}' loaded.", font.getName());
      }
   }

   private void initialiseApplication(final Stage primaryStage) throws Exception {
      loadFonts();
      readSettings();

      final List<Work> todaysWorkItems = model.getWorkRepository().findByCreationDate(LocalDate.now());
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
         globalScreenListener.shutdown(); // deregister, as this will keep app running
      });

      initialiseUI(primaryStage);
      initialisePopupUI(primaryStage);
   }

   private void readSettings() {
      LOG.debug("Reading configuration");

      final List<Settings> settingsList = model.getSettingsRepository().findAll();
      final Settings settings;
      if (settingsList.isEmpty()) {
         settings = new Settings();
         settings.setTaskBarColor(Model.TASK_BAR_COLOR.get());

         settings.setDefaultBackgroundColor(Model.ORIGINAL_DEFAULT_BACKGROUND_COLOR);
         settings.setDefaultFontColor(Model.ORIGINAL_DEFAULT_FONT_COLOR);

         settings.setHoverBackgroundColor(Model.ORIGINAL_HOVER_BACKGROUND_COLOR);
         settings.setHoverFontColor(Model.ORIGINAL_HOVER_Font_COLOR);
         settings.setUseHotkey(false);
         settings.setDisplayProjectsRight(false);
         settings.setHideProjectsOnMouseExit(true);
         model.getSettingsRepository().save(settings);
      } else {
         settings = settingsList.get(0);
      }

      Model.DEFAULT_BACKGROUND_COLOR.set(settings.getDefaultBackgroundColor());
      Model.DEFAULT_FONT_COLOR.set(settings.getDefaultFontColor());
      Model.HOVER_BACKGROUND_COLOR.set(settings.getHoverBackgroundColor());
      Model.HOVER_FONT_COLOR.set(settings.getHoverFontColor());
      Model.TASK_BAR_COLOR.set(settings.getTaskBarColor());
      Model.USE_HOTKEY.set(settings.isUseHotkey());
      Model.DISPLAY_PROJECTS_RIGHT.set(settings.isDisplayProjectsRight());
      Model.HIDE_PROJECTS_ON_MOUSE_EXIT.set(settings.isHideProjectsOnMouseExit());
   }

   private void initialisePopupUI(final Stage primaryStage) throws IOException {
      LOG.debug("Initialising popup UI.");

      globalScreenListener = new GlobalScreenListener();

      Model.USE_HOTKEY.addListener((a, b, newValue) -> globalScreenListener.register(newValue));
      globalScreenListener.register(Model.USE_HOTKEY.get());

      popupViewStage = new Stage();
      popupViewStage.initOwner(primaryStage);
      // Load root layout from fxml file.
      final FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Resources.getResource(RESOURCE.FXML_VIEW_POPUP_LAYOUT));
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
      viewControllerPopupController.setControllerAndModel(controller, model);
      globalScreenListener.setViewController(viewControllerPopupController);
   }

   private void initialiseUI(final Stage primaryStage) throws IOException {
      LOG.debug("Initialising main UI.");
      Pane mainPane;

      // Load root layout from fxml file.
      final FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Resources.getResource(RESOURCE.FXML_VIEW_LAYOUT));
      loader.setControllerFactory(springContext::getBean);
      mainPane = loader.load();
      primaryStage.initStyle(StageStyle.TRANSPARENT);
      // Show the scene containing the root layout.
      final Scene mainScene = new Scene(mainPane, Color.TRANSPARENT);

      // Image(Resources.getResource(RESOURCE.ICON_MAIN).toString())); // TODO use an app icon

      primaryStage.setTitle("KeepTime");
      primaryStage.setScene(mainScene);
      primaryStage.setAlwaysOnTop(true);
      primaryStage.setResizable(false);

      primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
         @Override
         public void handle(final WindowEvent event) {
            LOG.info("On close request");
         }
      });
      primaryStage.show();
      viewController = loader.getController();
      // Give the controller access to the main app.
      viewController.setStage(primaryStage);
      viewController.setController(controller, model);

   }

   @Override
   public void stop() throws Exception {
      springContext.stop();
   }

   public static void main(final String[] args) {
      launch(Main.class, args);
   }
}
