package de.doubleslash.keeptime.view.time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.DateFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Interval {

   private final static Logger Log = LoggerFactory.getLogger(Interval.class);

   public static List<CallBackListener> callBackListeners = new CopyOnWriteArrayList<>();

   private static Timeline timelineSession;
   private static LocalDateTime last = LocalDateTime.now();

   public static void registerCallBack(final CallBackListener cbl) {
      if (timelineSession == null) {
         createTimeLine();
      }
      callBackListeners.add(cbl);
   }

   /**
    * only create timeLine if needed
    */
   private static void createTimeLine() {
      timelineSession = new Timeline(new KeyFrame(Duration.seconds(1), (ae) -> {
         debounceAndExecuteCallbacks();
      }));
      timelineSession.setCycleCount(Animation.INDEFINITE);
      timelineSession.play();

   }

   private static void debounceAndExecuteCallbacks() {
      final LocalDateTime now = LocalDateTime.now();
      final long secondsBewtween = DateFormatter.getSecondsBewtween(last, now);
      final int nanoBetween = java.time.Duration.between(last, now).abs().getNano();

      // ignore callbacks faster than ~1 second (can happen if we were in standby)
      // Log.debug("{} - {} = {}.{}", now, last, secondsBewtween, nanoBetween);
      if (secondsBewtween >= 1 || nanoBetween > 900000000) {
         callBackListeners.forEach(CallBackListener::call);
         last = now;
      }
   }

   public static void removeCallBack(final CallBackListener cbl) {
      if (timelineSession == null) {
         return;
      }

      callBackListeners.remove(cbl);

      if (callBackListeners.isEmpty()) {
         timelineSession.stop();
         timelineSession = null;
      }
   }

}
