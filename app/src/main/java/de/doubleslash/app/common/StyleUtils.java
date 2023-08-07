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

package de.doubleslash.app.common;

public class StyleUtils {

   private StyleUtils() {

   }

   /**
    * Returns a new String representing a JavaFX style with a new given value for a given attribute.
    * 
    * @param style
    *           A String representing a JavaFX style.
    * @param attribute
    *           A String representing a JavaFX style attribute.
    * @param newValue
    *           A String representing the value that should be assigned to the given JavaFX style attribute.
    * @return A String representing the new JavaFX style.
    */
   public static String changeStyleAttribute(final String style, final String attribute, final String newValue) {
      final String newStyleAttribute = "-" + attribute + ": " + newValue + "; ";
      if (style.contains(attribute)) {
         return style.replaceAll("-" + attribute + ": " + "[^;]+;", newStyleAttribute);
      }
      return style + newStyleAttribute;
   }

}
