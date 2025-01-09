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


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectReportTest {

   /** The slf4j-logger for this class. */
   private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private static final String EMPTY_NOTE = "";

   private ProjectReport uut;

   @BeforeEach
   void setUp() {
      this.uut = new ProjectReport();
   }

   @Test
   void testAppendToWorkNotes_EmptyNoteAtStart() {
      this.uut.appendToWorkNotes(EMPTY_NOTE);
      this.uut.appendToWorkNotes("note 1 ");
      this.uut.appendToWorkNotes("note 2 ");
      final String expected = "note 1; note 2";
      assertEquals(expected, this.uut.getNotes());
   }

   @Test
   void testAppendToWorkNotes_EmptyNoteInTheMiddle() {
      this.uut.appendToWorkNotes("note 1 ");
      this.uut.appendToWorkNotes(EMPTY_NOTE);
      this.uut.appendToWorkNotes("note 2 ");
      final String expected = "note 1; note 2";
      assertEquals(expected, this.uut.getNotes());
   }

   @Test
   void testAppendToWorkNotes_NoEmptyNote() {
      this.uut = new ProjectReport();
      this.uut.appendToWorkNotes("note 1");
      this.uut.appendToWorkNotes("note 2");
      this.uut.appendToWorkNotes("note 3");
      final String expected = "note 1; note 2; note 3";
      assertEquals(expected, this.uut.getNotes());
   }

   @Test
   void testAppendToWorkNotes_EmptyNotesAtTheEnd() {
      this.uut = new ProjectReport();
      this.uut.appendToWorkNotes("note 1");
      this.uut.appendToWorkNotes("note 2");
      this.uut.appendToWorkNotes(EMPTY_NOTE);
      this.uut.appendToWorkNotes(EMPTY_NOTE);
      final String expected = "note 1; note 2";
      assertEquals(expected, this.uut.getNotes());
   }

}
