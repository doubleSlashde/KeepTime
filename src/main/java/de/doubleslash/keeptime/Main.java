package de.doubleslash.keeptime;

import java.io.IOException;
import java.time.LocalDate;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@SpringBootApplication
public class Main extends Application {

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   public static final String VERSION = "v0.0.2";

   private ConfigurableApplicationContext springContext;

   private Stage popupViewStage;

   private Model model;
   private Controller controller;

   @Override
   public void init() throws Exception {
      log.info("Starting KeepTime {}", VERSION);
      final DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();
      defaultExceptionHandler.register();

      springContext = SpringApplication.run(Main.class);
      // TODO test if everywhere is used the same model
      model = springContext.getBean(Model.class);
      controller = springContext.getBean(Controller.class);
   }

   @Override
   public void start(final Stage primaryStage) throws Exception {

      log.debug("Reading configuration");

      // TODO there should just be one instance of settings in the repo
      final List<Settings> settingsList = model.getSettingsRepository().findAll();
      final Settings settings;
      if (settingsList.isEmpty()) {
         settings = new Settings();
         settings.setTaskBarColor(Model.TASK_BAR_COLOR.get());

         settings.setDefaultBackgroundColor(Model.DEFAULT_BACKGROUND_COLOR.get());
         settings.setDefaultFontColor(Model.DEFAULT_FONT_COLOR.get());

         settings.setHoverBackgroundColor(Model.HOVER_BACKGROUND_COLOR.get());
         settings.setHoverFontColor(Model.HOVER_FONT_COLOR.get());
         settings.setUseHotkey(false);
         settings.setDisplayProjectsRight(true);
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

      final List<Work> todaysWorkItems = model.getWorkRepository().findByCreationDate(LocalDate.now());
      log.info("Found {} past work items", todaysWorkItems.size());
      model.getPastWorkItems().addAll(todaysWorkItems);

      final List<Project> projects = model.getProjectRepository().findAll();

      log.debug("Found '{}' projects", projects.size());
      if (projects.isEmpty()) {
         log.info("Adding default project");
         projects.add(Model.DEFAULT_PROJECT);
         model.getProjectRepository().save(Model.DEFAULT_PROJECT);
      }

      model.getAllProjects().addAll(projects);
      model.getAvailableProjects()
            .addAll(model.getAllProjects().stream().filter(Project::isEnabled).collect(Collectors.toList()));

      // set default project
      final Optional<Project> findAny = projects.stream().filter(p -> p.isDefault()).findAny();
      if (findAny.isPresent()) {
         model.setIdleProject(findAny.get());
         log.debug("Using project '{}' as default project.", model.getIdleProject());
      }

      primaryStage.setOnHiding(we -> {
         popupViewStage.close();
         globalScreenListener.register(false); // deregister, as this will keep app running
      });

      try {
         initialiseUI(primaryStage);
      } catch (final Exception e) {
         log.error(e.getMessage());
      }

      try {
         initialisePopupUI(primaryStage);
      } catch (final Exception e) {
         log.error(e.getMessage());
      }
   }

   GlobalScreenListener globalScreenListener;

   private void initialisePopupUI(final Stage primaryStage) throws IOException {
      // TODO register only if it is enabled
      globalScreenListener = new GlobalScreenListener();
      // TODO stop and close stage when main stage is shutdown
      Model.USE_HOTKEY.addListener((a, b, newValue) -> globalScreenListener.register(newValue));
      globalScreenListener.register(Model.USE_HOTKEY.get());

      // Platform.setImplicitExit(false); // TODO maybe not needed as other view will be available
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
      viewControllerPopupController.setController(controller, model);
      globalScreenListener.setViewController(viewControllerPopupController);

   }

   ViewController viewController;

   private void initialiseUI(final Stage primaryStage) {
      try {
         Pane mainPane;

         // Load root layout from fxml file.
         final FXMLLoader loader = new FXMLLoader();
         loader.setLocation(Resources.getResource(RESOURCE.FXML_VIEW_LAYOUT));
         loader.setControllerFactory(springContext::getBean);
         mainPane = loader.load();
         primaryStage.initStyle(StageStyle.TRANSPARENT);
         // Show the scene containing the root layout.
         final Scene mainScene = new Scene(mainPane, Color.TRANSPARENT);

         primaryStage.setTitle("KeepTime");
         primaryStage.setScene(mainScene);
         primaryStage.setAlwaysOnTop(true);
         primaryStage.setResizable(false);

         primaryStage.setOnCloseRequest(actionEvent -> log.info("On close request"));

         viewController = loader.getController();
         // Give the controller access to the main app.
         viewController.setStage(primaryStage);

         viewController.setController(controller, model);

         primaryStage.show();

      } catch (

      final Exception e) {
         log.error("Error: " + e.toString(), e);
      }
   }

   @Override
   public void stop() throws Exception {
      springContext.stop();
   }

   public static void main(final String[] args) {
      launch(Main.class, args);
   }
}
