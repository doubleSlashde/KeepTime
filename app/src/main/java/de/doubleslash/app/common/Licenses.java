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

package de.doubleslash.app.common;

public enum Licenses {

   GPLV3(
         "./licenses/GNU General Public License (GPL), Version 3.0.txt",
         "GNU General Public License Version 3.0",
         "https://www.gnu.org/licenses/gpl-3.0.de.html"),
   EPLV1("./licenses/EPL 1.0.txt", "Eclipse Public License 1.0", "https://www.eclipse.org/legal/epl-v10.html"),
   APACHEV2(
         "./licenses/Apache License, Version 2.0.txt",
         "Apache License Version 2.0",
         "https://www.apache.org/licenses/LICENSE-2.0"),
   LGPLV3(
         "./licenses/GNU Lesser General Public License (LGPL), Version 3.0.txt",
         "GNU Lesser General Public License Version 3.0",
         "https://www.gnu.org/licenses/lgpl-3.0.de.html"),
   MIT("./licenses/The MIT License.txt", "The MIT License", "https://opensource.org/licenses/MIT"),

   CC_4_0("./licenses/CC BY 4.0 License.txt", "CC BY 4.0 License", "https://creativecommons.org/licenses/by/4.0/");

   private final String path;
   private final String name;
   private final String url;

   private Licenses(final String licensePath, final String licenseName, final String urlWebsite) {
      path = licensePath;
      name = licenseName;
      url = urlWebsite;
   }

   public String getPath() {
      return path;
   }

   public String getName() {
      return name;
   }

   public String getUrl() {
      return url;
   }
}
