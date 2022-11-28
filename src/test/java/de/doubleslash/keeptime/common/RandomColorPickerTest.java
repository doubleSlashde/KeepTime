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

        Project p = new Project();
        p.setColor(Color.RED);

        RandomColorPicker.colors=Arrays.asList(Color.RED,Color.BLUE);

        Assertions.assertNotEquals(p.getColor().toString(),RandomColorPicker.getUniqueColor(List.of(p),Color.BLUE).toString());

    }
    @Test
    public void shouldReturnBlackWhenAllColorsTakenAlready(){
        Project p = new Project();
        p.setColor(Color.RED);

        RandomColorPicker.colors= List.of(Color.RED);

        Assertions.assertEquals(Color.BLACK,RandomColorPicker.getUniqueColor(List.of(p),Color.BLUE));
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