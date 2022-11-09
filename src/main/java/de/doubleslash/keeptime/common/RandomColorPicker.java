package de.doubleslash.keeptime.common;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RandomColorPicker {
    private static final Logger LOG = LoggerFactory.getLogger(RandomColorPicker.class);
    public static String getRandomColorName() throws ClassNotFoundException, IllegalAccessException {
        List<String> colorNames = new RandomColorPicker().getRandomColorNameList();
        double v = Math.random() * (colorNames.size() - 0);
        return colorNames.get((int) v);
    }


    private List getRandomColorNameList() throws ClassNotFoundException, IllegalAccessException {
        List<String> colorNames = new ArrayList<>();
        Class clazz = Class.forName("javafx.scene.paint.Color");
        if (clazz != null) {
            Field[] field = clazz.getFields();
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                Object colorObj = f.get(null);
                colorNames.add(String.valueOf(f).split("Color.")[2]);
            }
        }
        return colorNames;
    }

    public static String getUniqueColorName(String color) throws ClassNotFoundException, IllegalAccessException {

        List<String> colorNames = new RandomColorPicker().getRandomColorNameList();

        double v = Math.random() * (colorNames.size() - 0);
        if(color.equals(colorNames.get((int) v))){
            return colorNames.get((int)v+5);
        }
        return colorNames.get((int) v);
    }
}
