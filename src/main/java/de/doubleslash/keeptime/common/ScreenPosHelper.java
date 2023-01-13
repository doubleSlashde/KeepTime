package de.doubleslash.keeptime.common;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * Helper to handle screen coordinates. Converts between absolute coordinates to screen+proportional coordinates. E.g.
 * you have two screens, each with 1000px width. Absolute position gives you 1500px and this class will get you hash for
 * screen #2 and proportional value 0.5. <br>
 * Reference #38
 */
public class ScreenPosHelper {

   private static final Logger LOG = LoggerFactory.getLogger(ScreenPosHelper.class);

   private int screenHash;

   private double absoluteX;
   private double absoluteY;

   private double proportionalX;
   private double proportionalY;

   public ScreenPosHelper(final int screenhash, final double proportionalX, final double proportionalY) {
      this.screenHash = screenhash;
      this.proportionalX = proportionalX;
      this.proportionalY = proportionalY;
      calcAbsolutePosition();
   }

   public ScreenPosHelper(final double absoluteX, final double absoluteY) {
      this.absoluteX = absoluteX;
      this.absoluteY = absoluteY;
      calcProportionalPosition();
   }

   public double getAbsoluteX() {
      return absoluteX;
   }

   public void setAbsoluteX(final double absolutX) {
      this.absoluteX = absolutX;
      calcProportionalPosition();
   }

   public double getAbsoluteY() {
      return absoluteY;
   }

   public void setAbsoluteY(final double absolutY) {
      this.absoluteY = absolutY;
      calcProportionalPosition();
   }

   public int getScreenHash() {
      return screenHash;
   }

   public double getProportionalX() {
      return proportionalX;
   }

   public double getProportionalY() {
      return proportionalY;
   }

   public void resetPositionIfInvalid() {
      if (!isPositionValid()) {
         final Screen screen = getScreenWithHashOrPrimary(this.screenHash);
         final Rectangle2D screenBounds = screen.getBounds();
         LOG.warn(
               "Position is not in the range of the screen. Screen (hash '{}') range '{}'. Calculated positions '{}'/'{}'. Setting to '0'/'0'.",
               screen.hashCode(), screenBounds, absoluteX, absoluteY);
         proportionalX = 0;
         proportionalY = 0;
         absoluteX = 0;
         absoluteY = 0;
      }
   }

   private void calcProportionalPosition() {
      Screen screen = Screen.getPrimary();
      for (final Screen s : Screen.getScreens()) {
         if (s.getBounds().contains(this.absoluteX, this.absoluteY)) {
            screen = s;
            break;
         }
      }
      this.screenHash = screen.hashCode();
      final Rectangle2D bounds = screen.getBounds();
      final double xInScreen = absoluteX - bounds.getMinX();
      this.proportionalX = xInScreen / bounds.getWidth();
      final double yInScreen = absoluteY - bounds.getMinY();
      this.proportionalY = yInScreen / bounds.getHeight();

   }

   private void calcAbsolutePosition() {
      final Screen screen = getScreenWithHashOrPrimary(this.screenHash);
      final Rectangle2D screenBounds = screen.getBounds();

      this.absoluteX = screenBounds.getMinX() + (screenBounds.getWidth() * this.proportionalX);
      this.absoluteY = screenBounds.getMinY() + (screenBounds.getHeight() * this.proportionalY);
   }

   private boolean isPositionValid() {
      final Screen screen = getScreenWithHashOrPrimary(this.screenHash);
      final Rectangle2D screenBounds = screen.getBounds();
      return screenBounds.contains(this.absoluteX, this.absoluteY);
   }

   private Screen getScreenWithHashOrPrimary(final int screenHash) {
      for (final Screen s : Screen.getScreens()) {
         if (s.hashCode() == screenHash) {
            return s;
         }
      }
      LOG.warn("Could not find wanted screen with hash '{}'. Using primary.", screenHash);
      return Screen.getPrimary();
   }

}
