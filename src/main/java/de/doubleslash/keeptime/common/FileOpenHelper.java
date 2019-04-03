// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOpenHelper {

   private static final Logger LOG = LoggerFactory.getLogger(FileOpenHelper.class);

   private FileOpenHelper() {}

   public static void openFile(final String fileString) {
      final File file = new File(fileString);
      final Runtime rt = Runtime.getRuntime();

      if (OS.isWindows()) {
         executeCommandWindows(rt, file);
      } else if (OS.isLinux()) {
         executeCommandLinux(rt, file);
      } else {
         LOG.warn("OS is not supported");
      }
   }

   private static void executeCommandWindows(final Runtime rt, final File file) {
      try {
         final String command = "rundll32 url.dll,FileProtocolHandler " + file;
         LOG.debug("executing command: {}", command);

         rt.exec(command);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private static void executeCommandLinux(final Runtime rt, final File file) {
      try {
         final String command = "xdg-open " + file;
         LOG.debug("executing command: {}", command);

         rt.exec(command);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }
}
