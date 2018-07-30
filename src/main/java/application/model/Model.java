package application.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class Model {

   public final Project DEFAULT_PROJECT = new Project("Idle", Color.ORANGE, false, true);

   public ObservableList<Project> availableProjects = FXCollections.observableArrayList();

   public ObservableList<Work> pastWorkItems = FXCollections.observableArrayList();

   public ObjectProperty<Work> activeWorkItem = new SimpleObjectProperty<>();

   public long neededWorkSeconds = 60 * 60 * 8; // 8 hours

   public Project idleProject = DEFAULT_PROJECT;
   public Color taskBarColor = Color.BLACK;

   private final Color hoverBackgroundColor = new Color(54 / 255., 143 / 255., 179 / 255., .1);
   private final Color hoverFontColor = Color.BLACK;
   private final Color defaultBackgroundColor = new Color(54 / 255., 143 / 255., 179 / 255., 0.01);
   private final Color defaultFontColor = Color.BLACK;

}
