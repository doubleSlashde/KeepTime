package de.doubleslash.keeptime.common;

public enum Licenses {

   GPLV3("./licenses/GNU General Public License (GPL), Version 3.0.txt", "GNU General Public License Version 3.0"),
   EPLV1("./licenses/EPL 1.0.txt", "Eclipse Public License 1.0"),
   APACHEV2("./licenses/Apache License, Version 2.0.txt", "Apache License Version 2.0"),
   LGPLV3(
         "./licenses/GNU Lesser General Public License (LGPL), Version 3.0.txt",
         "GNU Lesser General Public License Version 3.0"),
   MIT("./licenses/The MIT License.txt", "The MIT License");

   private final String path;
   private final String name;

   Licenses(final String licensePath, final String licenseName) {
      path = licensePath;
      name = licenseName;
   }

   public String getPath() {
      return path;
   }

   public String getName() {
      return name;
   }
}
