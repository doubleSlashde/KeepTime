package de.doubleslash.keeptime.common;


import ch.qos.logback.core.pattern.color.BlueCompositeConverter;
import de.doubleslash.keeptime.model.Project;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.paint.Color;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;


class RandomColorPickerTest {
    private static final Logger LOG = LoggerFactory.getLogger(RandomColorPickerTest.class);

    @Test
    public void shouldGetARandomColor() {
      // Color color =   RandomColorPicker.getRandomColor();
      // Assertions.assertTrue(color!=null);
    }

    @Test
    public void shouldGetUniqueColor() {

        // RED, BLUE
        // assert unique color is BLUE

        Project p = new Project();
        p.setColor(Color.RED);

        RandomColorPicker.colors=Arrays.asList(Color.RED,Color.BLUE);

        Assertions.assertNotEquals(p.getColor().toString(),RandomColorPicker.getUniqueColor(Arrays.asList(p),Color.BLUE).toString());

    }
    @Test
    public void shouldReturnBlackWhenAllColorsTakenAlready(){
        Project p = new Project();
        p.setColor(Color.RED);

        RandomColorPicker.colors=Arrays.asList(Color.RED);

        Assertions.assertEquals(Color.BLACK,RandomColorPicker.getUniqueColor(Arrays.asList(p),Color.BLUE));
    }

    @Test
    public void shouldReturnUniqueColorWhenGiveBackgroundAndProjectColor(){
        Project p = new Project();
        p.setColor(Color.RED);
        Project p1 = new Project();
        p1.setColor(Color.GREEN);

        RandomColorPicker.colors=new ArrayList<>(Arrays.asList(Color.RED,Color.BLUE,Color.GREEN,Color.GRAY));

        Assertions.assertEquals(Color.GRAY,RandomColorPicker.getUniqueColor(Arrays.asList(p,p1),Color.BLUE));
    }




}