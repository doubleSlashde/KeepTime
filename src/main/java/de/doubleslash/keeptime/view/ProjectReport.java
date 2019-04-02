// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.view;

import static de.doubleslash.keeptime.view.ReportController.EMPTY_NOTE;
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
      if (!currentWorkNote.equals(EMPTY_NOTE)) {
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

   public String getNotes(final boolean addNumberOfNotes) {
      if (addNumberOfNotes) {
         return Integer.toString(this.numberOfNotes) + " Notes: " + this.sb.toString();
      } else {
         return this.sb.toString();
      }
   }

}
