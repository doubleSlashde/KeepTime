package de.doubleslash.keeptime.view.worktable;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.layout.HBox;

public class ProjectTableRow implements TableRow {

   private final Project project;
   private final long projectWorkSeconds;
   private final HBox buttonBox;

   public ProjectTableRow(final Project project, final long projectWorkSeconds, final HBox buttonBox) {
      this.projectWorkSeconds = projectWorkSeconds;
      this.project = project;
      this.buttonBox = buttonBox;
   }

   @Override
   public String getNotes() {
      return project.getName();
   }

   @Override
   public String getTimeRange() {
      return null;

   }

   @Override
   public String getTimeSum() {
      return DateFormatter.secondsToHHMMSS(projectWorkSeconds);

   }

   @Override
   public HBox getButtonBox() {
      return buttonBox;
   }

}
