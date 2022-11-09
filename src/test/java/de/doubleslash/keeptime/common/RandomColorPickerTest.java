package de.doubleslash.keeptime.common;


import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.view.ProjectsListViewController;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class RandomColorPickerTest {
    private static final Logger LOG = LoggerFactory.getLogger(RandomColorPickerTest.class);
    @Test
    public void shouldGetRandomColor() throws ClassNotFoundException, IllegalAccessException {

       String randomColor=  RandomColorPicker.getRandomColorName();
       LOG.info(randomColor);
       Assertions.assertTrue(!randomColor.isEmpty());
    }

    @Test
    public void colorIsNotAlreadyUsedByProject() throws ClassNotFoundException, IllegalAccessException {

        for (int i=0; i<1000; i++){

            String color = RandomColorPicker.getUniqueColorName("BLUE");
            Assertions.assertNotEquals("BLUE", color);
        }

    }

}