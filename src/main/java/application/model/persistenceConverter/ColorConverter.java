package application.model.persistenceConverter;

import javax.persistence.AttributeConverter;

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
