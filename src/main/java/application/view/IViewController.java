package application.view;

import javafx.scene.paint.Color;

public interface IViewController {

   public void setCurrentProjectTime();

   public void setOverallWorkTime();

   public void setOverallTime();

   public void setCurrentProject(String projectName, Color projectColor);

   public String getCurrentComments();

   public void updateProjectTimes(Object singleProjectTimes);
}
