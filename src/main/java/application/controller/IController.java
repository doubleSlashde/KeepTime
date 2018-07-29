package application.controller;

import java.time.LocalDate;

import application.model.Project;
import javafx.scene.paint.Color;

public interface IController {
   public void changeProject(Project newProject, String notes);

   public void changeProject(Project newProject, String notes, int minusSeconds);

   public void addNewProject(String projectName, boolean isWork, Color projectColor);

   public Object getDetails(LocalDate date);

   /**
    * Colors for background and fonts
    */
   public void setColors(Object colors);

   public void shutdown();

}
