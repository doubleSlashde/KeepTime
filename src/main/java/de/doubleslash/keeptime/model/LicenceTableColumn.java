package de.doubleslash.keeptime.model;

public class LicenceTableColumn {
	private String licenceName;
	private String softwareName;
	
	public LicenceTableColumn(String licenceName, String softwareName) {
		this.licenceName = licenceName;
		this.softwareName = softwareName;
	}
	
	public String getLicenceName() {
		return licenceName;
	}
	
	public void setLicenceName(String licenceName) {
		this.licenceName = licenceName;
	}
	
	public String getSoftwareName() {
		return softwareName;
	}
	
	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}
}
