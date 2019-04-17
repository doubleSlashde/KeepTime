// Copyright 2019 doubleSlash Net Business GmbH
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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOpenHelper {

   private static final Logger LOG = LoggerFactory.getLogger(FileOpenHelper.class);

   private FileOpenHelper() {}

   public static boolean openFile(final String fileString) {
      final File file = new File(fileString);
      final Runtime rt = Runtime.getRuntime();

      if (file.exists() && file.isFile()) {
         if (OS.isWindows()) {
            executeCommandWindows(rt, file);
         } else if (OS.isLinux()) {
            executeCommandLinux(rt, fileString);
         } else {
            LOG.warn("OS is not supported");
         }
         return true;
      } else {
         return false;
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

   private static void executeCommandLinux(final Runtime rt, String fileString) {
      fileString = fileString.replace(" ", "\\ ");
      try {
         final String command = "xdg-open " + fileString;
         LOG.debug("executing command: {}", command);

         rt.exec(command);
      } catch (final Exception e) {
         LOG.error(e.getMessage());
      }
   }
}
