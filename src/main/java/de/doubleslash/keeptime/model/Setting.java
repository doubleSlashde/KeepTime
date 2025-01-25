package de.doubleslash.keeptime.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Settings")
public class Setting {

   @Id
   @Column(nullable = false, unique = true)
   private String setting;

   @Lob
   private String settingValue;

   public Setting(){
      // for hibernate
   }

   public Setting(final String setting, final String settingValue) {
      this.setting = setting;
      this.settingValue = settingValue;
   }

   public String getSetting() {
      return setting;
   }

   public void setSetting(final String setting) {
      this.setting = setting;
   }

   public String getSettingValue() {
      return settingValue;
   }

   public void setSettingValue(final String settingValue) {
      this.settingValue = settingValue;
   }

}
