// Copyright 2019 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.view.time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.doubleslash.keeptime.common.DateFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Interval {

   private static final List<CallBackListener> callBackListeners = new CopyOnWriteArrayList<>();

   private static Timeline timelineSession;
   private static LocalDateTime last = LocalDateTime.now();

   private Interval() {
      throw new IllegalStateException("Utility class");
   }

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
      timelineSession = new Timeline(new KeyFrame(Duration.seconds(1), ae -> debounceAndExecuteCallbacks()));
      timelineSession.setCycleCount(Animation.INDEFINITE);
      timelineSession.play();

   }

   private static void debounceAndExecuteCallbacks() {
      final LocalDateTime now = LocalDateTime.now();
      final long secondsBewtween = DateFormatter.getSecondsBewtween(last, now);
      final int nanoBetween = java.time.Duration.between(last, now).abs().getNano();

      // ignore callbacks faster than ~1 second (can happen if we were in standby)
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
