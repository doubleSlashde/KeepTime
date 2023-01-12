package de.doubleslash.keeptime.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javafx.scene.paint.Color;

class RandomColorPickerTest {
   @Test
   public void shouldReturnBlackWhenAllColorsTakenAlready() {
      RandomColorPicker.colors = List.of(Color.RED);

      Assertions.assertEquals(Color.BLACK, RandomColorPicker.getUniqueColor(List.of(Color.RED), Color.BLUE, Color.YELLOW));
   }

   @Test
   public void shouldReturnUniqueColorWhenGiveBackgroundAndProjectColor() {
      RandomColorPicker.colors = new ArrayList<>(Arrays.asList(Color.RED, Color.BLUE, Color.GREEN, Color.GRAY));

      Assertions.assertEquals(Color.GRAY,
            RandomColorPicker.getUniqueColor(Arrays.asList(Color.RED, Color.GREEN), Color.BLUE,Color.YELLOW));
   }

}