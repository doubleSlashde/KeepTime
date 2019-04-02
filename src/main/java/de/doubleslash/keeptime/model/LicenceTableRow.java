package de.doubleslash.keeptime.model;

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
