// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.common;

public class OS {

   private static final String OS_PROPERTY = "os.name";

   private OS() {

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
