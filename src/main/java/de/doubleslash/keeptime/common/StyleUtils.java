package de.doubleslash.keeptime.common;

public class StyleUtils {

   private StyleUtils() {

   }

   public static String changeStyleAttribute(final String style, final String attribute, final String newValue) {
      String newStyle = "";
      final String newStyleAttribute = "-" + attribute + ": " + newValue + "; ";
      if (style.contains(attribute)) {
         newStyle = style.replaceAll("-" + attribute + ": " + "[^;]+;", newStyleAttribute);
      } else {
         newStyle = style + newStyleAttribute;
      }

      return newStyle;
   }

}
