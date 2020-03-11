
package de.doubleslash.keeptime.view.worktable;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.model.Work;
import javafx.scene.layout.HBox;

public class WorkTableRow implements TableRow {
   private final Work work;
   private final HBox buttonBox;

   public WorkTableRow(final Work work, final HBox buttonBox) {
      this.work = work;
      this.buttonBox = buttonBox;
   }

   @Override
   public String getNotes() {
      return work.getNotes();
   }

   @Override
   public String getTimeRange() {
      return DateFormatter.toTimeString(work.getStartTime()) + " - " + DateFormatter.toTimeString(work.getEndTime());

   }

   @Override
   public String getTimeSum() {
      return DateFormatter.secondsToHHMMSS(DateFormatter.getSecondsBewtween(work.getStartTime(), work.getEndTime()));

   }

   @Override
   public HBox getButtonBox() {
      return buttonBox;
   }

   @Override
   public boolean isUnderlined() {
      return false;
   }

}
