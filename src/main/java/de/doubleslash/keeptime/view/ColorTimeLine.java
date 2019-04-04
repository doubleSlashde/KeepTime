package de.doubleslash.keeptime.view;

import java.time.Duration;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Work;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ColorTimeLine {

   private final Canvas canvas;
   private final Model model;
   private final Controller controller;

   public ColorTimeLine(final Canvas canvas, final Model model, final Controller controller) {
      this.canvas = canvas;
      this.model = model;
      this.controller = controller;
   }

   public void update() {
      final GraphicsContext gc = canvas.getGraphicsContext2D();

      gc.setFill(new Color(.3, .3, .3, .3));
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

      double currentX = 0;
      final long maxSeconds = controller.calcTodaysSeconds();
      for (final Work w : model.getPastWorkItems()) {
         final long workedSeconds = Duration.between(w.getStartTime(), w.getEndTime()).getSeconds();
         final double fillX = (float) workedSeconds / maxSeconds * canvas.getWidth();
         gc.setFill(w.getProject().getColor());
         gc.fillRect(currentX, 0, fillX, canvas.getHeight());
         currentX += fillX;
      }
   }

}
