// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserHelper {

   public static final Logger LOG = LoggerFactory.getLogger(BrowserHelper.class);

   private BrowserHelper() {}

   public static void openURL(final String url) {
      final Runtime rt = Runtime.getRuntime();

      if (OS.isWindows()) {
         executeCommandWindows(rt, url);
      } else if (OS.isLinux()) {
         executeCommandLinux(rt, url);
      } else {
         LOG.warn("OS is not supported");
      }
   }

   private static void executeCommandWindows(final Runtime rt, final String url) {
      try {
         final String command = "rundll32 url.dll,FileProtocolHandler " + url;
         LOG.debug("execute command: {}", command);

         rt.exec(command);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private static void executeCommandLinux(final Runtime rt, final String url) {
      try {
         final String command = "xdg-open " + url;
         LOG.debug("execute command: {}", command);

         rt.exec(command);
      } catch (final Exception e) {
         LOG.warn(e.getMessage());
      }
   }
}
