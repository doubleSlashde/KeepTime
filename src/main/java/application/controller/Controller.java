package application.controller;

import java.time.LocalDate;

import javafx.scene.paint.Color;

public interface Controller {
   public void changeProject(int projectId);

   public void changeProject(int projectId, int minusSeconds);

   public Object getDetails(LocalDate date);

   public void addProject(int projectName, boolean isWork, Color projectColor);

   /**
    * Colors for background and fonts
    */
   public void setColors(Object colors);

   public Object getColors();

   public Object getProjects();
}
