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

   private Licenses(final String licensePath, final String licenseName) {
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
