package de.doubleslash.keeptime.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.paint.Color;

public class RandomColorPicker {
   private static final Logger LOG = LoggerFactory.getLogger(RandomColorPicker.class);

   public static List<Color> colors = new ArrayList<>();

   static {
      try {
         fillColorList();
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   public static Color getRandomColor(List<Color> colorList) {

      int rnd = new Random().nextInt(colorList.size());
      return colorList.get(rnd);
   }

   private static void fillColorList() throws IllegalAccessException {
      Field[] field = Color.class.getFields();
      if (field != null) {
         for (int i = 0; i < field.length; i++) {
            Field f = field[i];
            Object obj = f.get(null);
            if (obj instanceof Color) {
               colors.add((Color) obj);
            }
         }
      }
   }

   public static Color chooseContrastColor(List<Color> availableColors, Color backgroundColor,Color hoverbackgroundColor) {

      double divAdd = 0;
      Color divColor = null;
      int maxTries = 0;

      while (divAdd < 1 && maxTries < 10) {
         divColor = getUniqueColor(availableColors, backgroundColor, hoverbackgroundColor);
         double divred = Math.abs(divColor.getRed() - backgroundColor.getRed());
         double divgreen = Math.abs(divColor.getGreen() - backgroundColor.getGreen());
         double divblue = Math.abs(divColor.getBlue() - backgroundColor.getBlue());
         divAdd = divblue + divgreen + divred;
         maxTries++;
      }
      return divColor;
   }

   public static Color getUniqueColor(List<Color> availableColors, Color backgroundColor, Color hoverbackgroundColor) {

      List<Color> uniqueColorList = new ArrayList<>(colors);
      uniqueColorList.remove(backgroundColor); // List should remove all already used colors.
      uniqueColorList.remove(hoverbackgroundColor);
      for (Color color : availableColors) {
         for (int i = 0; i < uniqueColorList.size(); i++) {

            if (color.toString().equals(uniqueColorList.get(i).toString())) {
               uniqueColorList.remove(i);
            }
         }
      }
      if(uniqueColorList.isEmpty()){
         LOG.info("Empty uniqueColorList return Black");
         return Color.BLACK;
      }
      return getRandomColor(uniqueColorList);
   }

}
