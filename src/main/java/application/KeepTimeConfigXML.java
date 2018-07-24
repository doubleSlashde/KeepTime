package application;

import java.util.ArrayList;
import java.util.List;

import application.model.Project;
import javafx.scene.paint.Color;

public class KeepTimeConfigXML {

   public Color taskBarColor;

   public List<Project> projects = new ArrayList<>();

   public Project idleProject;

   public KeepTimeConfigXML() {
      projects.add(idleProject);
   }
}
