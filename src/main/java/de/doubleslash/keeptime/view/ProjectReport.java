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

package de.doubleslash.keeptime.view;

import static de.doubleslash.keeptime.view.ReportController.NOTE_DELIMETER;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectReport {

   /** The slf4j-logger for this class. */
   private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private int numberOfNotes;

   private final int size;

   private final StringBuilder sb;

   public ProjectReport(final int size) {
      this.size = size;
      this.sb = new StringBuilder(2 * 1024);
   }

   public void appendToWorkNotes(final String currentWorkNote) {
      this.numberOfNotes++;
      if (!currentWorkNote.isEmpty()) {
         if (this.numberOfNotes > 1) {
            this.sb.append(NOTE_DELIMETER);
         }
         this.sb.append(currentWorkNote.trim());
      } else {
         LOG.debug("Skipping empty note.");
      }
   }

   public int getNumberOfNotes() {
      return this.numberOfNotes;
   }

   public String getNotes() {
      return this.sb.toString();
   }

}
