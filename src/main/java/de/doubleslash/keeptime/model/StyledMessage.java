// Copyright 2025 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a styled text message composed of multiple text segments. This class provides a UI-agnostic way to
 * represent formatted text, allowing separation of business logic from UI components.
 */
public class StyledMessage {

   /**
    * Represents a single text segment with optional styling.
    *
    * @param text
    *       The text content
    * @param bold
    *       Whether the text should be displayed in bold
    */
   public record TextSegment(String text, boolean bold) {
      public TextSegment(String text) {
         this(text, false);
      }
   }

   private final List<TextSegment> segments;

   public StyledMessage(List<TextSegment> segments) {
      this.segments = new ArrayList<>(segments);
   }

   /**
    * Creates a StyledMessage from a variable number of text segments.
    *
    * @param segments
    *       The text segments to include in the message
    * @return A new StyledMessage containing the provided segments
    */
   public static StyledMessage of(TextSegment... segments) {
      return new StyledMessage(List.of(segments));
   }

   public List<TextSegment> getSegments() {
      return new ArrayList<>(segments);
   }

   /**
    * Returns the message as plain text without styling.
    */
   public String toPlainText() {
      return segments.stream().map(TextSegment::text).reduce("", String::concat);
   }
}

