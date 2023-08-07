// Copyright 2019 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.view.worktable;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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

   @Override
   public boolean isUnderlined() {
      return project.isWork();
   }

   @Override
   public Color getProjectColor() {
      return project.getColor();
   }

}
