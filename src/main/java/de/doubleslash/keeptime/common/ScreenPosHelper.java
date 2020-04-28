package de.doubleslash.keeptime.common;

import javafx.stage.Screen;

public class ScreenPosHelper {

   private int screenhash;
   private double absolutX;
   private double absolutY;
   private double xProportion;
   private double yProportion;

   public ScreenPosHelper(final double absolutX, final double absolutY) {
      this.absolutX = absolutX;
      this.absolutY = absolutY;
      calcRelativScreenpos();
   }

   public ScreenPosHelper(final int screenhash, final double xProportion, final double yProportion) {
      this.screenhash = screenhash;
      this.xProportion = xProportion;
      this.yProportion = yProportion;
      calcAbsolutScreenpos();
   }

   public double getAbsolutX() {
      return absolutX;
   }

   public void setAbsolutX(final double absolutX) {
      this.absolutX = absolutX;
      calcRelativScreenpos();
   }

   public double getAbsolutY() {
      return absolutY;
   }

   public void setAbsolutY(final double absolutY) {
      this.absolutY = absolutY;
      calcRelativScreenpos();
   }

   public int getScreenhash() {
      return screenhash;
   }

   public void setScreenhash(final int screenhash) {
      this.screenhash = screenhash;
      calcRelativScreenpos();
   }

   public double getxProportion() {
      return xProportion;
   }

   public void setxProportion(final double xProportion) {
      this.xProportion = xProportion;
      calcAbsolutScreenpos();
   }

   public double getyProportion() {
      return yProportion;
   }

   public void setyProportion(final double yProportion) {
      this.yProportion = yProportion;
      calcAbsolutScreenpos();
   }

   private void calcRelativScreenpos() {
      Screen screen = Screen.getPrimary();
      for (final Screen s : Screen.getScreens()) {
         if (s.getBounds().getMinX() <= this.absolutX && this.absolutX <= s.getBounds().getMaxX()
               && s.getBounds().getMinY() <= this.absolutY && this.absolutY <= s.getBounds().getMaxY()) {
            screen = s;
            break;
         }
      }
      this.screenhash = screen.hashCode();
      final double xInScreen = absolutX - screen.getBounds().getMinX();
      this.xProportion = xInScreen / screen.getBounds().getWidth();
      final double yInScreen = absolutY - screen.getBounds().getMinY();
      this.yProportion = yInScreen / screen.getBounds().getHeight();

   }

   private void calcAbsolutScreenpos() {
      final Screen screen = getScreenWithHashOrPrimary(this.screenhash);
      this.absolutX = screen.getBounds().getMinX() + (screen.getBounds().getWidth() * this.xProportion);
      this.absolutY = screen.getBounds().getMinY() + (screen.getBounds().getHeight() * this.yProportion);
   }

   private Screen getScreenWithHashOrPrimary(final int screenHash) {
      for (final Screen s : Screen.getScreens()) {
         if (s.hashCode() == screenHash) {
            return s;
         }
      }
      return Screen.getPrimary();
   }

}
