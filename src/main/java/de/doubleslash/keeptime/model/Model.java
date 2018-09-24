package de.doubleslash.keeptime.model;

import java.util.Comparator;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.paint.Color;

@Component
public class Model {
   public static final Color originalHoverBackgroundColor = (SystemUtils.IS_OS_LINUX)
         ? new Color(54 / 255., 143 / 255., 179 / 255., 1.)
         : new Color(54 / 255., 143 / 255., 179 / 255., 0.01);
   public static final Color originalHoverFontColor = Color.BLACK;
   public static final Color originalDefaultBackgroundColor = (SystemUtils.IS_OS_LINUX)
         ? new Color(54 / 255., 143 / 255., 179 / 255., 1.)
         : new Color(54 / 255., 143 / 255., 179 / 255., .1);
   public static final Color originalDefaultFontColor = Color.BLACK;

   public static final Color originalTaskBarFontColor = Color.BLACK;

   public final Project DEFAULT_PROJECT = new Project("Idle", Color.ORANGE, false, 0, true);
   public Project idleProject = DEFAULT_PROJECT;

   public ObservableList<Project> availableProjects = FXCollections.observableArrayList();
   public SortedList<Project> sortedAvailableProjects = new SortedList<>(availableProjects,
         Comparator.comparing(Project::getIndex));
   public ObservableList<Project> allProjects = FXCollections.observableArrayList();

   public ObservableList<Work> pastWorkItems = FXCollections.observableArrayList();
   public ObjectProperty<Work> activeWorkItem = new SimpleObjectProperty<>();

   public long neededWorkSeconds = 60 * 60 * 8; // 8 hours

   public ObjectProperty<Color> taskBarColor = new SimpleObjectProperty<>(originalTaskBarFontColor);

   public ObjectProperty<Color> hoverBackgroundColor = new SimpleObjectProperty<>(originalHoverBackgroundColor);
   public ObjectProperty<Color> hoverFontColor = new SimpleObjectProperty<>(originalHoverFontColor);
   public ObjectProperty<Color> defaultBackgroundColor = new SimpleObjectProperty<>(originalDefaultBackgroundColor);
   public ObjectProperty<Color> defaultFontColor = new SimpleObjectProperty<>(originalDefaultFontColor);
   public ObjectProperty<Boolean> useHotkey = new SimpleObjectProperty<>(false);
   public ObjectProperty<Boolean> displayProjectsRight = new SimpleObjectProperty<>(false);

}
