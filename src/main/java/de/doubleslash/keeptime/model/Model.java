package de.doubleslash.keeptime.model;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.SettingsRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.paint.Color;

@Component
public class Model {
   private ProjectRepository projectRepository;
   private WorkRepository workRepository;
   private SettingsRepository settingsRepository;

   @Autowired
   public Model(final ProjectRepository projectRepository, final WorkRepository workRepository,
         final SettingsRepository settingsRepository) {
      super();
      this.projectRepository = projectRepository;
      this.workRepository = workRepository;
      this.settingsRepository = settingsRepository;
   }

   public static final Color ORIGINAL_HOVER_BACKGROUND_COLOR = new Color(54 / 255., 143 / 255., 179 / 255., .7);

   public static final Color ORIGINAL_HOVER_Font_COLOR = Color.BLACK;
   public static final Color ORIGINAL_DEFAULT_BACKGROUND_COLOR = new Color(54 / 255., 143 / 255., 179 / 255., .7);
   public static final Color ORIGINAL_DEFAULT_FONT_COLOR = Color.BLACK;

   public static final Color ORIGINAL_TASK_BAR_FONT_COLOR = Color.BLACK;

   public static final Project DEFAULT_PROJECT = new Project("Idle", Color.ORANGE, false, 0, true);
   private Project idleProject = DEFAULT_PROJECT;

   private final ObservableList<Project> availableProjects = FXCollections.observableArrayList();
   private final SortedList<Project> sortedAvailableProjects = new SortedList<>(availableProjects,
         Comparator.comparing(Project::getIndex));
   private ObservableList<Project> allProjects = FXCollections.observableArrayList();

   protected static final ObservableList<Work> pastWorkItems = FXCollections.observableArrayList();
   public static final ObjectProperty<Work> activeWorkItem = new SimpleObjectProperty<>();

   public static final ObjectProperty<Color> TASK_BAR_COLOR = new SimpleObjectProperty<>(ORIGINAL_TASK_BAR_FONT_COLOR);

   public static final ObjectProperty<Color> HOVER_BACKGROUND_COLOR = new SimpleObjectProperty<>(
         ORIGINAL_HOVER_BACKGROUND_COLOR);
   public static final ObjectProperty<Color> HOVER_FONT_COLOR = new SimpleObjectProperty<>(ORIGINAL_HOVER_Font_COLOR);
   public static final ObjectProperty<Color> DEFAULT_BACKGROUND_COLOR = new SimpleObjectProperty<>(
         ORIGINAL_DEFAULT_BACKGROUND_COLOR);
   public static final ObjectProperty<Color> DEFAULT_FONT_COLOR = new SimpleObjectProperty<>(
         ORIGINAL_DEFAULT_FONT_COLOR);
   public static final ObjectProperty<Boolean> USE_HOTKEY = new SimpleObjectProperty<>(false);
   public static final ObjectProperty<Boolean> DISPLAY_PROJECTS_RIGHT = new SimpleObjectProperty<>(false);
   public static final ObjectProperty<Boolean> HIDE_PROJECTS_ON_MOUSE_EXIT = new SimpleObjectProperty<>(true);

   public void setWorkRepository(final WorkRepository workRepository) {
      this.workRepository = workRepository;
   }

   public void setProjectRepository(final ProjectRepository projectRepository) {
      this.projectRepository = projectRepository;
   }

   public void setSettingsRepository(final SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;
   }

   public void setIdleProject(final Project idleProject) {
      this.idleProject = idleProject;
   }

   public void setAllProjects(final ObservableList<Project> allProjects) {
      this.allProjects = allProjects;
   }

   public WorkRepository getWorkRepository() {
      return workRepository;
   }

   public SortedList<Project> getSortedAvailableProjects() {
      return sortedAvailableProjects;
   }

   public ProjectRepository getProjectRepository() {
      return projectRepository;
   }

   public SettingsRepository getSettingsRepository() {
      return settingsRepository;
   }

   public ObservableList<Work> getPastWorkItems() {
      return pastWorkItems;
   }

   public Project getIdleProject() {
      return idleProject;
   }

   public ObservableList<Project> getAvailableProjects() {
      return availableProjects;
   }

   public ObservableList<Project> getAllProjects() {
      return allProjects;
   }
}
