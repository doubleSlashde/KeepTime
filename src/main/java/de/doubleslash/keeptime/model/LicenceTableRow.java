package de.doubleslash.keeptime.model;

public class LicenceTableRow {
   private String name;
   private String license;

   public LicenceTableRow(final String softwareName, final String licenceName) {
      this.license = licenceName;
      this.name = softwareName;
   }

   public String getName() {
      return name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public String getLicense() {
      return license;
   }

   public void setLicense(final String license) {
      this.license = license;
   }
}
