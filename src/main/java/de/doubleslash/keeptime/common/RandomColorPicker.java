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
                if(obj instanceof Color){
                    colors.add((Color) obj);
                }
            }
        }
    }

    public static Color chooseContrastColor(Color backgroundColor, ObservableList<Project> availableProjects) throws IllegalAccessException {

        double divAdd=0;
        Color divColor=null;
        int maxTrys=0;

        while (divAdd <1 && maxTrys<10) {
            divColor = getUniqueColor(availableProjects,backgroundColor);
            double divred = Math.abs(divColor.getRed()-backgroundColor.getRed());
            double divgreen = Math.abs(divColor.getGreen()-backgroundColor.getGreen());
            double divblue = Math.abs(divColor.getBlue()- backgroundColor.getBlue());
            divAdd  = divblue+divgreen+divred;
            maxTrys++;
        }
        return divColor;
    }

    public static Color getUniqueColor(List<Project> availableProjects, Color backgroundColor) {

        List<Color> uniqueColorList = colors;
        uniqueColorList.remove(backgroundColor); //List should remove all already used colors.

           for(Project project : availableProjects){
                for (int i=0; i<uniqueColorList.size(); i++){

                    if (project.getColor().toString().equals(uniqueColorList.get(i).toString())) {
                        uniqueColorList.remove(i);
                    }
                }
           }
           return getRandomColor(uniqueColorList);
       }

}
