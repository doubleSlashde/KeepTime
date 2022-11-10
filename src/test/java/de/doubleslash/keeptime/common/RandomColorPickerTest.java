package de.doubleslash.keeptime.common;


import ch.qos.logback.core.pattern.color.BlueCompositeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.paint.Color;


class RandomColorPickerTest {
    private static final Logger LOG = LoggerFactory.getLogger(RandomColorPickerTest.class);

    @Test
    public void shouldGetARandomColor() throws ClassNotFoundException, IllegalAccessException {
       Color color =   RandomColorPicker.getRandomColor();
       Assertions.assertTrue(color!=null);
    }


}