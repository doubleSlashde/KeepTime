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

package de.doubleslash.keeptime.common;

import javafx.scene.paint.Color;

import java.util.Random;

public class ColorHelper {

   private static final Random random = new Random();

   private ColorHelper() {
      throw new IllegalStateException("Utility class: ColorHelper");
   }

   public static Color randomColor() {
      double hue = random.nextDouble() * 360;
      double saturation = 0.7 + random.nextDouble() * 0.3; // High saturation
      double brightness = 0.8 + random.nextDouble() * 0.2; // High brightness
      return Color.hsb(hue, saturation, brightness);
   }

   public static String colorToCssRgba(final Color color) {
      return colorToCssRgb(color) + ", " + color.getOpacity();
   }

   public static String colorToCssRgb(final Color color) {
      return color.getRed() * 255 + ", " + color.getGreen() * 255 + ", " + color.getBlue() * 255;
   }
}
