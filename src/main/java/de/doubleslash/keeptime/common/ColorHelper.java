package de.doubleslash.keeptime.common;

import javafx.scene.paint.Color;

public class ColorHelper {

   public static Color randomColor() {
      return Color.BLACK;
   }

   public static String colorToCssRgba(final Color color) {
      return colorToCssRgb(color) + ", " + color.getOpacity();
   }

   public static String colorToCssRgb(final Color color) {
      return color.getRed() * 255 + ", " + color.getGreen() * 255 + ", " + color.getBlue() * 255;
   }
}
