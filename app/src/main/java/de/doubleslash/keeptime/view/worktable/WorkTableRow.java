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
import de.doubleslash.keeptime.model.Work;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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

   @Override
   public Color getProjectColor() {
      return null;
   }

}
