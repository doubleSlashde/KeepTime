package application.viewPopup;

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
   private final org.slf4j.Logger Log = LoggerFactory.getLogger(this.getClass());

   private ViewControllerPopup viewController;

   public GlobalScreenListener() {
      try {
         GlobalScreen.registerNativeHook();
      } catch (final NativeHookException ex) {
         Log.error("Could not register native hooks.", ex);
      }
      setGlobalScreenLogLevel();

      GlobalScreen.addNativeKeyListener(this);
      GlobalScreen.addNativeMouseListener(this);
      GlobalScreen.addNativeMouseMotionListener(this);
      GlobalScreen.addNativeMouseWheelListener(this);

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
   boolean shiftPressed = false;

   private Point mouseLocation = new Point(0, 0);

   @Override
   public void nativeKeyPressed(final NativeKeyEvent e) {
      final String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());

      switch (e.getKeyCode()) {
      case NativeKeyEvent.VC_CONTROL:
         controllPressed = true;
         break;
      case NativeKeyEvent.VC_SHIFT:
         shiftPressed = true;
         break;
      default:
      }

      if (controllPressed && shiftPressed) {
         Platform.runLater(() -> {
            viewController.show(mouseLocation);
         });
      }
   }

   @Override
   public void nativeKeyReleased(final NativeKeyEvent e) {
      switch (e.getKeyCode()) {
      case NativeKeyEvent.VC_CONTROL:
         controllPressed = false;
         break;
      case NativeKeyEvent.VC_SHIFT:
         shiftPressed = false;
         break;
      default:
      }

      if (!controllPressed || !shiftPressed) {
         // System.out.println("Hide");
         // Platform.runLater(() -> viewController.hide());
      }
   }

   @Override
   public void nativeKeyTyped(final NativeKeyEvent e) {}

   @Override
   public void nativeMouseClicked(final NativeMouseEvent e) {}

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
