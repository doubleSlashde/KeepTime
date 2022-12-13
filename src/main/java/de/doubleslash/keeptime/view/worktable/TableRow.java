package de.doubleslash.keeptime.view.worktable;

import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public interface TableRow {

   public String getNotes();

   public String getTimeRange();

   public String getTimeSum();

   public HBox getButtonBox();

   public boolean isUnderlined();

   public Color getProjectColor();
}
