package de.doubleslash.keeptime.common;




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
import java.util.Random;

public class RandomColorPicker {
    private static final Logger LOG = LoggerFactory.getLogger(RandomColorPicker.class);

   public static Color getRandomColor() throws IllegalAccessException {

        List<Color> colors = new ArrayList<>();
        Field[] field = Color.class.getFields();
        if (field != null) {
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                Object obj = f.get(null);
                if(obj instanceof Color){
                    colors.add((Color) obj);
                }
            }
        }
        int rnd = new Random().nextInt(colors.size());
        return colors.get(rnd);
    }

    public static Color chooseContrastColor(Color backgroundColor, ObservableList<Project> availableProjects) throws IllegalAccessException {

        double divAdd=0;
        Color divColor=null;

        while (divAdd <1) {
            divColor = getUniqueColor(availableProjects);
            double divred = Math.abs(divColor.getRed()-backgroundColor.getRed());
            double divgreen = Math.abs(divColor.getGreen()-backgroundColor.getGreen());
            double divblue = Math.abs(divColor.getBlue()- backgroundColor.getBlue());
            divAdd  = divblue+divgreen+divred;

        }
        return divColor;
    }

    public static Color getUniqueColor(ObservableList<Project> availableProjects) throws IllegalAccessException {

       Color color = getRandomColor();
       int tempInt=0;
       while(true){

           for(Project project : availableProjects){

               if (project.getColor().toString().equals(color.toString())) {
                   tempInt++;
                   break;
               }
           }
           if(tempInt==0){
               return color;
           }
           color = getRandomColor();
           tempInt=0;
       }
    }
}
