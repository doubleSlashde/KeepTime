package de.doubleslash.keeptime.viewpopup;

import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

public class GlobalScreenListener implements NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener {
   private static final int LEFT_WINDOWS = 91;

   private static final int LEFT_CTRL = 162;

   private final org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());

   private ViewControllerPopup viewController;

   public GlobalScreenListener() {

      setGlobalScreenLogLevel();

      GlobalScreen.addNativeKeyListener(this);
      GlobalScreen.addNativeMouseListener(this);
      GlobalScreen.addNativeMouseMotionListener(this);
      GlobalScreen.addNativeMouseWheelListener(this);
   }

   public void register(final boolean register) {
      try {
         if (register) {
            log.info("Registering native hook");
            GlobalScreen.registerNativeHook();
         } else {
            log.info("Unregistering native hook");
            GlobalScreen.unregisterNativeHook();
         }
      } catch (final NativeHookException ex) {
         log.error("Error whill (un)registering natvice hooks.", ex);
      }
   }

   public void setGlobalScreenLogLevel() {
      // Get the logger for "org.jnativehook" and set the level to off.
      final Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());

      logger.setLevel(Level.OFF);

      // Don't forget to disable the parent handlers
      logger.setUseParentHandlers(false);
   }

   public void setViewController(final ViewControllerPopup viewController) {
      this.viewController = viewController;
   }

   boolean controllPressed = false;
   boolean windowsPressed = false;

   private Point mouseLocation = new Point(0, 0);

   @Override
   public void nativeKeyPressed(final NativeKeyEvent e) {
      // TODO find better hotkey - maybe configurable?

      switch (e.getRawCode()) {
      case LEFT_CTRL:
         controllPressed = true;
         break;
      case LEFT_WINDOWS:
         windowsPressed = true;
         break;
      default:
      }

      if (controllPressed && windowsPressed) {
         Platform.runLater(() -> viewController.show(mouseLocation));
      }
   }

   @Override
   public void nativeKeyReleased(final NativeKeyEvent e) {
      switch (e.getRawCode()) {
      case LEFT_CTRL:
         controllPressed = false;
         break;
      case LEFT_WINDOWS:
         windowsPressed = false;
         break;
      default:
      }
   }

   @Override
   public void nativeKeyTyped(final NativeKeyEvent e) {
      // Do nothing because ... I have no clue. Nico what is this?
   }

   @Override
   public void nativeMouseClicked(final NativeMouseEvent e) {
      // And here the same. I don't know. Nico you can change these :D
   }

   @Override
   public void nativeMousePressed(final NativeMouseEvent nativeEvent) {
      // TODO Auto-generated method stub

   }

   @Override
   public void nativeMouseReleased(final NativeMouseEvent nativeEvent) {
      // TODO Auto-generated method stub

   }

   @Override
   public void nativeMouseMoved(final NativeMouseEvent nativeEvent) {
      mouseLocation = nativeEvent.getPoint();
   }

   @Override
   public void nativeMouseDragged(final NativeMouseEvent nativeEvent) {
      // TODO Auto-generated method stub

   }

   @Override
   public void nativeMouseWheelMoved(final NativeMouseWheelEvent nativeEvent) {
      // TODO Auto-generated method stub

   }

}
