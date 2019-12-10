package de.doubleslash.keeptime.common;

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
