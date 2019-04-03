// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOpenHelper {

   private static final Logger LOG = LoggerFactory.getLogger(FileOpenHelper.class);

   private final File fileToOpen;

   public FileOpenHelper(final File file) {
      this.fileToOpen = file;
   }

   public FileOpenHelper(final String file) {
      this.fileToOpen = new File(file);
   }

   public void openFile() {
      executeCommand();
   }

   public static void openFile(final File file) {
      executeCommand(file);
   }

   public static void openFile(final String fileString) {
      final File file = new File(fileString);

      executeCommand(file);
   }

   private void executeCommand() {
      final Runtime rt = Runtime.getRuntime();

      if (OS.isWindows()) {
         executeCommandWindows(rt);
      } else if (OS.isLinux()) {
         executeCommandLinux(rt);
      } else {
         LOG.warn("OS is not supported");
      }
   }

   private static void executeCommand(final File file) {
      final Runtime rt = Runtime.getRuntime();

      if (OS.isWindows()) {
         executeCommandWindows(rt, file);
      } else if (OS.isLinux()) {
         executeCommandLinux(rt, file);
      } else {
         LOG.warn("OS is not supported");
      }
   }

   private void executeCommandWindows(final Runtime rt) {
      try {
         rt.exec("rundll32 url.dll,FileProtocolHandler " + fileToOpen);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private static void executeCommandWindows(final Runtime rt, final File file) {
      try {
         rt.exec("rundll32 url.dll,FileProtocolHandler " + file);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private void executeCommandLinux(final Runtime rt) {
      try {
         rt.exec("xdg-open " + fileToOpen);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private static void executeCommandLinux(final Runtime rt, final File file) {
      try {
         rt.exec("xdg-open " + file);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }
}
