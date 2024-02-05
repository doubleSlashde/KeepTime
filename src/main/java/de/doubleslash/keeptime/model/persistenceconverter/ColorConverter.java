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

package de.doubleslash.keeptime.model.persistenceconverter;

import jakarta.persistence.AttributeConverter;
import javafx.scene.paint.Color;

public class ColorConverter implements AttributeConverter<Color, String> {
   @Override
   public Color convertToEntityAttribute(final String arg0) {
      try {
         return Color.valueOf(arg0);
      } catch (final Exception e) {
         return Color.BLACK;
      }
   }

   @Override
   public String convertToDatabaseColumn(final Color arg0) {
      return arg0.toString();
   }
}
