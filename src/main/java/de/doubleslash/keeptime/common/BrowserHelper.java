// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserHelper {

   public static final Logger LOG = LoggerFactory.getLogger(BrowserHelper.class);

   private URL urlToOpen;

   public BrowserHelper() {}

   public BrowserHelper(final String url) {
      this.urlToOpen = getURLfromString(url);
   }

   public BrowserHelper(final URL url) {
      this.urlToOpen = url;
   }

   public void openURL(final URL url) {
      executeCommand(url);
   }

   public void openURL(final String urlString) {
      final URL url = getURLfromString(urlString);

      executeCommand(url);
   }

   public void openURL() {
      executeCommand();
   }

   public void setURL(final URL url) {
      this.urlToOpen = url;
   }

   public void setURL(final String url) {
      this.urlToOpen = getURLfromString(url);
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

   private void executeCommand(final URL url) {
      final Runtime rt = Runtime.getRuntime();

      if (OS.isWindows()) {
         executeCommandWindows(rt, url);
      } else if (OS.isLinux()) {
         executeCommandLinux(rt, url);
      } else {
         LOG.warn("OS is not supported");
      }
   }

   private void executeCommandWindows(final Runtime rt) {
      try {
         rt.exec("rundll32 url.dll,FileProtocolHandler " + urlToOpen);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private void executeCommandWindows(final Runtime rt, final URL url) {
      try {
         rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private void executeCommandLinux(final Runtime rt) {
      try {
         rt.exec("xdg-open " + urlToOpen);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }

   private void executeCommandLinux(final Runtime rt, final URL url) {
      try {
         rt.exec("xdg-open " + url);
      } catch (final Exception e) {
         LOG.warn(e.getMessage());
      }
   }

   private URL getURLfromString(final String urlString) {
      try {
         return new URL(urlString);
      } catch (final MalformedURLException e) {
         LOG.error(e.getMessage());
      }

      return null;
   }
}
