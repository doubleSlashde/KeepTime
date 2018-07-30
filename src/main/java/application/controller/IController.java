package application.controller;

import java.time.LocalDate;

import application.model.Project;
import javafx.scene.paint.Color;

public interface IController {
   public void changeProject(Project newProject, String notes);

   public void changeProject(Project newProject, String notes, long minusSeconds);

   public void addNewProject(String projectName, boolean isWork, Color projectColor);

   public Object getDetails(LocalDate date);

   /**
    * Colors for background and fonts
    */
   public void setColors(Object colors);

   public void shutdown();

   public void renameProject(Project p, String newName);

   public void deleteProject(Project p);

   public void changeProjectColor(Project p, Color newColor);

}
