package de.doubleslash.keeptime.model.persistenceconverter;

import javafx.scene.paint.Color;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ColorGuiConverter implements AttributeConverter<Color, String> {

   @Override
   public String convertToDatabaseColumn(Color color) {
      return convertToString(color);
   }

   @Override
   public Color convertToEntityAttribute(String colorString) {
      return convertToColor(colorString);
   }

   public static String convertToString(Color color) {
      return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
   }

   public static Color convertToColor(String colorString) {
      return Color.web(colorString);
   }
}
