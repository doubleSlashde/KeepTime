// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

public class OS {

   private OS() {

   }

   public static boolean isWindows() {
      if (System.getProperty("os.name").toLowerCase().contains("windows")) {
         return true;
      }

      return false;
   }

   public static boolean isLinux() {
      if (System.getProperty("os.name").toLowerCase().contains("linux")) {
         return true;
      }

      return false;
   }

}
