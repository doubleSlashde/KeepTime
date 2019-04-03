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

package de.doubleslash.keeptime.view.license;

import de.doubleslash.keeptime.common.Licenses;

public class LicenceTableRow {
   private String name;
   private String licenseName;
   private Licenses license;

   public LicenceTableRow(final String name, final Licenses license) {
      this.license = license;
      this.licenseName = license.getName();
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public String getLicenseName() {
      return licenseName;
   }

   public void setLicenseName(final String licenseName) {
      this.licenseName = licenseName;
   }

   public Licenses getLicense() {
      return license;
   }

   public void setLicense(final Licenses license) {
      this.license = license;
   }
}
