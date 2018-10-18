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

import de.doubleslash.keeptime.common.Resources;
import de.doubleslash.keeptime.common.Resources.RESOURCE;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Settings;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.view.ViewController;
import de.doubleslash.keeptime.viewPopup.GlobalScreenListener;
import de.doubleslash.keeptime.viewPopup.ViewControllerPopup;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

@SpringBootApplication
public class Main extends Application {

   private final Logger LOG = LoggerFactory.getLogger(this.getClass());

   public static final String VERSION = "v0.0.2";

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
         initUI(primaryStage);
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

   private void initUI(final Stage primaryStage) throws Exception {

      readSettings();

      final List<Work> todaysWorkItems = model.workRepository.findByCreationDate(LocalDate.now());
      LOG.info("Found {} past work items", todaysWorkItems.size());
      model.pastWorkItems.addAll(todaysWorkItems);

      final List<Project> projects = model.projectRepository.findAll();

      LOG.debug("Found '{}' projects", projects.size());
      if (projects.isEmpty()) {
         LOG.info("Adding default project");
         projects.add(model.DEFAULT_PROJECT);
         model.projectRepository.save(model.DEFAULT_PROJECT);
      }

      model.allProjects.addAll(projects);
      model.availableProjects
            .addAll(model.allProjects.stream().filter(Project::isEnabled).collect(Collectors.toList()));

      // set default project
      final Optional<Project> findAny = projects.stream().filter(Project::isDefault).findAny();
      if (findAny.isPresent()) {
         model.idleProject = findAny.get();
         LOG.debug("Using project '{}' as default project.", model.idleProject);
      }

      primaryStage.setOnHiding((we) -> {
         popupViewStage.close();
         globalScreenListener.shutdown(); // deregister, as this will keep app running
      });

      initialiseUI(primaryStage);
      initialisePopupUI(primaryStage);
   }

   private void readSettings() {
      LOG.debug("Reading configuration");

      final List<Settings> settingsList = model.settingsRepository.findAll();
      final Settings settings;
      if (settingsList.isEmpty()) {
         settings = new Settings();
         settings.setTaskBarColor(model.taskBarColor.get());

         settings.setDefaultBackgroundColor(model.defaultBackgroundColor.get());
         settings.setDefaultFontColor(model.defaultFontColor.get());

         settings.setHoverBackgroundColor(model.hoverBackgroundColor.get());
         settings.setHoverFontColor(model.hoverFontColor.get());
         settings.setUseHotkey(false);
         settings.setDisplayProjectsRight(false);
         settings.setHideProjectsOnMouseExit(true);
         model.settingsRepository.save(settings);
      } else {
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
   }

   private void initialisePopupUI(final Stage primaryStage) throws IOException {
      LOG.debug("Initialising popup UI.");

      globalScreenListener = new GlobalScreenListener();
      // stop and close stage when main stage is shutdown
      model.useHotkey.addListener((a, b, newValue) -> {
         globalScreenListener.register(newValue);
      });
      globalScreenListener.register(model.useHotkey.get());

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
      viewControllerPopupController.setStage(popupViewStage, popupScene);
      viewControllerPopupController.setController(controller, model);
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

      // Image(Resources.getResource(RESOURCE.ICON_MAIN).toString()));

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

      viewController = loader.getController();
      // Give the controller access to the main app.
      viewController.setStage(primaryStage);
      viewController.setController(controller, model);

      primaryStage.show();
   }

   @Override
   public void stop() throws Exception {
      springContext.stop();
   }

   public static void main(final String[] args) {
      launch(Main.class, args);
   }
}
