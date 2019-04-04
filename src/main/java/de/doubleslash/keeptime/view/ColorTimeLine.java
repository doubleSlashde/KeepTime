package de.doubleslash.keeptime.view;

import java.time.Duration;
import java.util.List;

import de.doubleslash.keeptime.model.Work;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ColorTimeLine {

   private final Canvas canvas;

   public ColorTimeLine(final Canvas canvas) {
      this.canvas = canvas;
   }

   public void update(final List<Work> workItems, final long seconds) {
      final GraphicsContext gc = canvas.getGraphicsContext2D();

      gc.setFill(new Color(.3, .3, .3, .3));
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
      double currentX = 0;
      for (final Work w : workItems) {
         final long workedSeconds = Duration.between(w.getStartTime(), w.getEndTime()).getSeconds();
         final double width = (double) workedSeconds / seconds * canvas.getWidth();
         final Color fill = w.getProject().getColor();

         gc.setFill(fill);
         gc.fillRect(currentX, 0, width, canvas.getHeight());

         currentX += width;
      }

   }

}
