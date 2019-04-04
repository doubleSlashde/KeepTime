package de.doubleslash.keeptime.view;

import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ColorTimeLine {

   private final Canvas canvas;

   public ColorTimeLine(final Canvas canvas) {
      this.canvas = canvas;
   }

   public void update(final List<Rectangle> rects) {
      final GraphicsContext gc = canvas.getGraphicsContext2D();

      gc.setFill(new Color(.3, .3, .3, .3));
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

      for (final Rectangle rect : rects) {
         gc.setFill(rect.getFill());
         gc.fillRect(rect.getX(), 0, rect.getWidth(), canvas.getHeight());
      }

   }

}
