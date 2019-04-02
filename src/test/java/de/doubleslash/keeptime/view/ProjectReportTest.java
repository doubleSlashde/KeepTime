// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.view;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectReportTest {

   /** The slf4j-logger for this class. */
   private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private ProjectReport uut;

   @Before
   public void setUp() throws Exception {
      this.uut = new ProjectReport(3);
   }

   @Test
   public void testAppendToWorkNotes() {
      this.uut.appendToWorkNotes("note 1 ");
      this.uut.appendToWorkNotes(ReportController.EMPTY_NOTE);
      this.uut.appendToWorkNotes("note 2 ");
      final String expected = "note 1; note 2";
      assertEquals(expected, this.uut.getNotes(false));
   }

   @Test
   public void testAppendToWorkNotesAddNumberOfNotes() {
      this.uut.appendToWorkNotes("note 1 ");
      this.uut.appendToWorkNotes(ReportController.EMPTY_NOTE);
      this.uut.appendToWorkNotes("note 2 ");
      final String expected = "3 Notes: note 1; note 2";
      assertEquals(expected, this.uut.getNotes(true));
   }

   @Test
   public void testAppendToWorkNotesAddNumberOfNotes_2() {
      this.uut = new ProjectReport(3);
      this.uut.appendToWorkNotes("note 1");
      this.uut.appendToWorkNotes("note 2");
      this.uut.appendToWorkNotes("note 3");
      final String expected = "3 Notes: note 1; note 2; note 3";
      assertEquals(expected, this.uut.getNotes(true));
   }

   @Test
   public void testAppendToWorkNotesAddNumberOfNotes_EmptyNotesAtTheEnd() {
      this.uut = new ProjectReport(4);
      this.uut.appendToWorkNotes("note 1");
      this.uut.appendToWorkNotes("note 2");
      this.uut.appendToWorkNotes(ReportController.EMPTY_NOTE);
      this.uut.appendToWorkNotes(ReportController.EMPTY_NOTE);
      final String expected = "4 Notes: note 1; note 2";
      assertEquals(expected, this.uut.getNotes(true));
   }

}
