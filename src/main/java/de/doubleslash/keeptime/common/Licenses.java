package de.doubleslash.keeptime.common;

public enum Licenses {

   GPLV3("./licenses/GNU General Public License (GPL), Version 3.0.txt"),
   EPLV1("./licenses/EPL 1.0.txt"),
   APACHEV2("./licenses/Apache License, Version 2.0.txt"),
   LGPLV3("./licenses/GNU Lesser General Public License (LGPL), Version 3.0.txt"),
   MIT("./licenses/The MIT License.txt");

   private final String path;

   Licenses(final String licensePath) {
      path = licensePath;
   }

   public String getPath() {
      return path;
   }
}
