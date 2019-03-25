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
            final String[] browsers = {
                  "epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx"
            };

            final StringBuilder cmd = new StringBuilder();
            for (int i = 0; i < browsers.length; i++) {
               if (i == 0) {
                  cmd.append(String.format("%s \"%s\"", browsers[i], url));
               } else {
                  cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
               }
            }

            rt.exec(new String[] {
                  "sh", "-c", cmd.toString()
            });
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
