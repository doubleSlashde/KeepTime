package de.doubleslash.keeptime.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserHelper {
   public static final Logger LOG = LoggerFactory.getLogger(BrowserHelper.class);

   private BrowserHelper() {

   }

   public static void openURL(final String url) {
      final Runtime rt = Runtime.getRuntime();
      if (System.getProperty("os.name").contains("Windows")) {
         try {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
         } catch (final Exception e) {
            LOG.error(e.getMessage());
         }
      } else if (System.getProperty("os.name").contains("Linux")) {
         try {
            rt.exec("xdg-open " + url);
         } catch (final Exception e) {
            LOG.error(e.getMessage());
         }
      } else {
         try {
            rt.exec("open " + url);
         } catch (final Exception e) {
            LOG.error(e.getMessage());
         }
      }
   }
}
