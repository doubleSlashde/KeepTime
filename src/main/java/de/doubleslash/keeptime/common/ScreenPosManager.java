package de.doubleslash.keeptime.common;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Settings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ScreenPosManager {

   Stage stage;
   Model model;
   Controller controller;

   public ScreenPosManager(final Stage stage, final Model model, final Controller controller) {
      this.stage = stage;
      this.model = model;
      this.controller = controller;

      setStageToSavedPos();
      registerStageMovedListener();

   }

   public void setStageToSavedPos() {
      if (model.screenSettings.saveWindowPosition.get()) {
         stage.setX(getAbsolutePositionX(model));
         stage.setY(getAbsolutePositionY(model));
      }
   }

   public void registerStageMovedListener() {
      final ChangeListener<Number> posChange = new ChangeListener<Number>() {

         @Override
         public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
               final Number newValue) {
            if (!model.screenSettings.saveWindowPosition.get()) {
               return;
            }

            final Settings newSettings = new Settings(model.hoverBackgroundColor.get(), model.hoverFontColor.get(),
                  model.defaultBackgroundColor.get(), model.defaultFontColor.get(), model.taskBarColor.get(),
                  model.useHotkey.get(), model.displayProjectsRight.get(), model.hideProjectsOnMouseExit.get(),
                  model.screenSettings.windowPositionX.get(), model.screenSettings.windowPositionY.get(),
                  model.screenSettings.screenHash.get(), model.screenSettings.saveWindowPosition.get(),
                  model.remindIfNotesAreEmpty.get());

            if (observable.equals(stage.xProperty())) {
               Screen screen = Screen.getPrimary();
               for (final Screen s : Screen.getScreens()) {
                  if (s.getVisualBounds().getMinX() <= newValue.doubleValue()
                        && newValue.doubleValue() <= s.getVisualBounds().getMaxX()
                        && s.getVisualBounds().getMinY() <= model.screenSettings.windowPositionY.get()
                        && model.screenSettings.windowPositionY.get() <= s.getVisualBounds().getMaxY()) {
                     screen = s;
                     break;
                  }
               }
               newSettings.setScreenHash(screen.hashCode());
               final double xInScreen = newValue.doubleValue() - screen.getVisualBounds().getMinX();
               newSettings.setWindowPositionX(xInScreen / screen.getVisualBounds().getWidth());

            } else {

               Screen screen = Screen.getPrimary();
               for (final Screen s : Screen.getScreens()) {
                  if (s.getVisualBounds().getMinY() <= newValue.doubleValue()
                        && newValue.doubleValue() <= s.getVisualBounds().getMaxY()
                        && s.getVisualBounds().getMinX() <= model.screenSettings.windowPositionX.get()
                        && model.screenSettings.windowPositionX.get() <= s.getVisualBounds().getMaxX()) {
                     screen = s;
                     break;
                  }
               }
               newSettings.setScreenHash(screen.hashCode());
               final double yInScreen = newValue.doubleValue() - screen.getVisualBounds().getMinY();
               newSettings.setWindowPositionY(yInScreen / screen.getVisualBounds().getHeight());
            }

            controller.updateSettings(newSettings);

         }

      };
      stage.xProperty().addListener(posChange);
      stage.yProperty().addListener(posChange);

   }

   private double getAbsolutePositionX(final Model model) {
      final Screen toDisplay = getScreenWithHashOrPrimary(model.screenSettings.screenHash.get());
      return toDisplay.getVisualBounds().getMinX()
            + (toDisplay.getVisualBounds().getWidth() * model.screenSettings.windowPositionX.get());

   }

   private double getAbsolutePositionY(final Model model) {
      final Screen toDisplay = getScreenWithHashOrPrimary(model.screenSettings.screenHash.get());
      return toDisplay.getVisualBounds().getMinY()
            + (toDisplay.getVisualBounds().getHeight() * model.screenSettings.windowPositionY.get());

   }

   private Screen getScreenWithHashOrPrimary(final int hash) {

      for (final Screen s : Screen.getScreens()) {
         if (s.hashCode() == hash) {
            return s;
         }
      }
      return Screen.getPrimary();
   }

}
