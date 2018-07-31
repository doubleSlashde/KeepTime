package application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import application.common.Resources;
import application.common.Resources.RESOURCE;
import application.controller.Controller;
import application.controller.IController;
import application.model.Model;
import application.model.Project;
import application.model.Work;
import application.model.repos.ProjectRepository;
import application.model.repos.WorkRepository;
import application.view.ViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@SpringBootApplication
public class Main extends Application {

   private ConfigurableApplicationContext springContext;

   public static Stage stage;

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   private final Model model = new Model(); // TODO init model
   private IController controller;

   public static ProjectRepository projectRepo;

   public static WorkRepository workRepository;

   @Override
   public void init() throws Exception {
      springContext = SpringApplication.run(Main.class);
   }

   @Override
   public void start(final Stage primaryStage) throws Exception {
      stage = primaryStage;

      Log.debug("Reading configuration");

      projectRepo = springContext.getBean(ProjectRepository.class);
      workRepository = springContext.getBean(WorkRepository.class);
      final List<Work> todaysWorkItems = workRepository.findByCreationDate(LocalDate.now());
      Log.info("Found {} past work items", todaysWorkItems.size());
      model.pastWorkItems.addAll(todaysWorkItems);

      final List<Project> projects = projectRepo.findAll();

      Log.debug("Found '{}' projects", projects.size());
      for (final Project project : projects) {
         Log.info(project.toString());
      }
      if (projects.isEmpty()) {
         Log.info("Adding default project");
         projects.add(model.DEFAULT_PROJECT);
         projectRepo.save(model.DEFAULT_PROJECT);
      }

      // createProjects();

      model.allProjects.addAll(projects);
      model.availableProjects
            .addAll(model.allProjects.stream().filter(Project::isEnabled).collect(Collectors.toList()));

      // set default project
      final Optional<Project> findAny = projects.stream().filter(p -> p.isDefault()).findAny();
      if (findAny.isPresent()) {
         model.idleProject = findAny.get();
         Log.debug("Using project '{}' as default project.", model.idleProject);
      }

      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            // Not on FX thread!
            // shutdown();
         }
      });

      primaryStage.setOnCloseRequest((we) -> {
         shutdown(); // spring cant save on shutdown hook
         System.exit(0);
      });

      try {
         initialiseUI(primaryStage);
      } catch (final Exception e) {
         e.printStackTrace();
      }
   }

   private void createProjects() {
      projectRepo.save(new Project("G&D", Color.BLUE, true));
      projectRepo.save(new Project("G&D AR", Color.WHITE, true));
      projectRepo.save(new Project("ZF 3d", Color.PINK, true));
      projectRepo.save(new Project("Karl Storz", Color.GREEN, true));
      projectRepo.save(new Project("Fronius", Color.VIOLET, true));
      projectRepo.save(new Project("Kicker", Color.YELLOW, true));
      projectRepo.save(new Project("Mitagessen", Color.RED, true));
      projectRepo.save(new Project("Zeppelin", Color.ORANGE, true));
      projectRepo.save(new Project("Other", Color.ORANGE, true));

   }

   private void shutdown() {
      Log.info("Shutting down");
      viewController.changeProject(model.idleProject, 0);
      controller.shutdown();
      // springContext.close();
   }

   ViewController viewController;

   private void initialiseUI(final Stage primaryStage) {
      try {
         Pane loginLayout;

         // Load root layout from fxml file.
         final FXMLLoader loader = new FXMLLoader();
         loader.setLocation(Resources.getResource(RESOURCE.FXML_VIEW_LAYOUT));
         loader.setControllerFactory(springContext::getBean);
         loginLayout = loader.load();
         primaryStage.initStyle(StageStyle.TRANSPARENT);
         // Show the scene containing the root layout.
         final Scene loginScene = new Scene(loginLayout, Color.TRANSPARENT);

         // Image(Resources.getResource(RESOURCE.ICON_MAIN).toString()));

         primaryStage.setTitle("KeepTime");
         primaryStage.setScene(loginScene);
         // Give the controller access to the main app.
         primaryStage.setAlwaysOnTop(true);
         viewController = loader.getController();
         viewController.setStage(primaryStage);

         controller = new Controller(model);
         viewController.setController(controller, model);

         primaryStage.show();

      } catch (final Exception e) {
         Log.error("Error: " + e.toString(), e);
         e.printStackTrace();
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
