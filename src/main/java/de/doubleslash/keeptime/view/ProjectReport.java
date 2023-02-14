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
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectReport {

   /** The slf4j-logger for this class. */
   private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final StringJoiner sb;

   public ProjectReport() {
      this.sb = new StringJoiner(NOTE_DELIMETER);
   }

   public void appendToWorkNotes(final String currentWorkNote) {

      if (!currentWorkNote.isEmpty()) {
         this.sb.add(currentWorkNote.trim());
      } else {
         LOG.debug("Skipping empty note.");
      }
   }

   public String getNotes() {
      return sb.toString();
   }

}
