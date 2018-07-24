package application.model;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class KeepTimeConfigXML {

   public Color taskBarColor;

   public List<Project> projects = new ArrayList<>();

   public Project idleProject;

   public KeepTimeConfigXML() {
      projects.add(idleProject);
   }
}
