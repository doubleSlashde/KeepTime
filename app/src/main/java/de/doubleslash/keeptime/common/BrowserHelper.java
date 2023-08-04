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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserHelper {

   public static final Logger LOG = LoggerFactory.getLogger(BrowserHelper.class);

   private BrowserHelper() {}

   public static void openURL(final String url) {
      final Runtime rt = Runtime.getRuntime();

      if (OS.isWindows()) {
         openUrlWindows(rt, url);
      } else if (OS.isLinux()) {
         openUrlLinux(rt, url);
      } else {
         LOG.warn("OS is not supported");
      }
   }

   private static void openUrlWindows(final Runtime rt, final String url) {
      final String command = "rundll32 url.dll,FileProtocolHandler " + url;
      try {
         LOG.debug("Executing command: {}", command);
         rt.exec(command);
      } catch (final Exception e) {
         LOG.error("Could not open url '" + url + "' with command '" + command + "'.", e);
      }
   }

   private static void openUrlLinux(final Runtime rt, final String url) {
      final String[] command = {
            "xdg-open", url
      };
      try {
         LOG.debug("Executing command: {}", Arrays.toString(command));
         rt.exec(command);
      } catch (final Exception e) {
         LOG.error("Could not open url '" + url + "' with command '" + Arrays.toString(command) + "'.", e);
      }
   }
}
