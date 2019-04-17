// Copyright 2019 doubleSlash Net Business GmbH.
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

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
         final String command = url;
         LOG.debug("execute command: xdg-open {}", command);

         rt.exec(new String[] {
               "xdg-open", command
         });
      } catch (final Exception e) {
         LOG.warn(e.getMessage());
      }
   }
}
