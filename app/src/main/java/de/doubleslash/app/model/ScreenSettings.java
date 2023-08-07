package de.doubleslash.app.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ScreenSettings {
   public final ObjectProperty<Boolean> saveWindowPosition = new SimpleObjectProperty<>(false);
   public final ObjectProperty<Double> proportionalX = new SimpleObjectProperty<>(0.5);
   public final ObjectProperty<Double> proportionalY = new SimpleObjectProperty<>(0.5);
   public final ObjectProperty<Integer> screenHash = new SimpleObjectProperty<>(0);
}
