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

public class OS {

   private static final String OS_PROPERTY = "os.name";

   private OS() {
      // prevent instance creation
   }

   public static boolean isWindows() {
      if (System.getProperty(OS_PROPERTY).toLowerCase().contains("windows")) {
         return true;
      }

      return false;
   }

   public static boolean isLinux() {
      if (System.getProperty(OS_PROPERTY).toLowerCase().contains("linux")) {
         return true;
      }

      return false;
   }

   public static String getOSname() {
      return System.getProperty(OS_PROPERTY);
   }

}
