package de.doubleslash.keeptime.model;

import de.doubleslash.keeptime.common.Licenses;

public class LicenceTableRow {
   private String name;
   private Licenses license;

   public LicenceTableRow(final String softwareName, final Licenses licenceName) {
      this.license = licenceName;
      this.name = softwareName;
   }

   public String getName() {
      return name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public Licenses getLicense() {
      return license;
   }

   public void setLicense(final Licenses license) {
      this.license = license;
   }
}
