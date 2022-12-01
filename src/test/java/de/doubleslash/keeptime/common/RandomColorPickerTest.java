package de.doubleslash.keeptime.common;


import de.doubleslash.keeptime.model.Project;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



class RandomColorPickerTest {

    @Test
    public void shouldGetUniqueColor() {

        // RED, BLUE
        // assert unique color is BLUE

        RandomColorPicker.colors=Arrays.asList(Color.RED,Color.BLUE);

        Assertions.assertNotEquals(Color.RED.toString(),RandomColorPicker.getUniqueColor(List.of(Color.RED),Color.BLUE).toString());

    }
    @Test
    public void shouldReturnBlackWhenAllColorsTakenAlready(){
        RandomColorPicker.colors= List.of(Color.RED);

        Assertions.assertEquals(Color.BLACK,RandomColorPicker.getUniqueColor(List.of(Color.RED),Color.BLUE));
    }

    @Test
    public void shouldReturnUniqueColorWhenGiveBackgroundAndProjectColor(){
        RandomColorPicker.colors=new ArrayList<>(Arrays.asList(Color.RED,Color.BLUE,Color.GREEN,Color.GRAY));

        Assertions.assertEquals(Color.GRAY,RandomColorPicker.getUniqueColor(Arrays.asList(Color.RED,Color.GREEN),Color.BLUE));
    }




}