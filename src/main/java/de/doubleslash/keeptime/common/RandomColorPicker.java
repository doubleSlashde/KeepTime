package de.doubleslash.keeptime.common;



import de.doubleslash.keeptime.Main;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Settings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Field;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

public class RandomColorPicker {
    private static final Logger LOG = LoggerFactory.getLogger(RandomColorPicker.class);

   public static Color getRandomColor() throws ClassNotFoundException, IllegalAccessException {

        List<Color> colors = new ArrayList<>();
        Class clazz = Class.forName("javafx.scene.paint.Color");
        if (clazz != null) {
            Field[] field = clazz.getFields();
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                Object obj = f.get(null);
                if(obj instanceof Color){
                    colors.add((Color) obj);
                }

            }
        }
        double v = Math.random() * (colors.size() - 0);
        return colors.get((int) v);
    }

    public static Color chooseContrastColor(Color backgroundColor, ObservableList<Project> availableProjects) throws ClassNotFoundException, IllegalAccessException {
         Color divColor = getUniqueColor(availableProjects);
        double divred = Math.abs(divColor.getRed()-backgroundColor.getRed());
        double divgreen = Math.abs(divColor.getGreen()-backgroundColor.getGreen());
        double divblue = Math.abs(divColor.getBlue()-backgroundColor.getBlue());

        double divAdd  = divblue+divgreen+divred;

        while (divAdd <0.6) {

             divColor = getUniqueColor(availableProjects);
             divred = Math.abs(divColor.getRed()-backgroundColor.getRed());
             divgreen = Math.abs(divColor.getGreen()-backgroundColor.getGreen());
             divblue = Math.abs(divColor.getBlue()- backgroundColor.getBlue());
             divAdd  = divblue+divgreen+divred;

        }
        return divColor;
    }

    public static Color getUniqueColor(ObservableList<Project> availableProjects) throws ClassNotFoundException, IllegalAccessException {

       Color color = getRandomColor();
       for(Project project : availableProjects){

           while (project.getColor().toString().equals(color.toString())) {
                color = getRandomColor();
           }
       }
        return color;
    }
}
