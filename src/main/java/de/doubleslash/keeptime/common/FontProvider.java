package de.doubleslash.keeptime.common;

import javafx.scene.text.Font;

public class FontProvider {
   private static final Font defaultFont = Font.font("Open Sans Regular");
   private static final Font boldFont = Font.font("Open Sans Bold");

   private FontProvider() {
      // no instances allowed
   }

   public static Font getDefaultFont() {
      return defaultFont;
   }

   public static Font getBoldFont() {
      return boldFont;
   }
}
