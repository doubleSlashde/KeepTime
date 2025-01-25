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

package de.doubleslash.keeptime.model.settings;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class HeimatSettings {

   private final SettingsBase settingsBase;

   public HeimatSettings(SettingsBase settingsBase) {
      this.settingsBase = settingsBase;
   }

   public boolean isHeimatActive() {
      return settingsBase.getBoolean("heimat_active", false);
   }

   public void setHeimatActive(boolean heimatActive) {
      settingsBase.setBoolean("heimat_active", heimatActive);
   }

   public String getHeimatUrl() {
      return settingsBase.getString("heimat_url", "");
   }

   public void setHeimatUrl(String heimatUrl) {
      settingsBase.setString("heimat_url", heimatUrl);
   }

   public String getHeimatPat() {
      return settingsBase.getString("heimat_pat", "");
   }

   public void setHeimatPat(String heimatPat) {
      settingsBase.setString("heimat_pat", heimatPat);
   }

   public void save() {
      settingsBase.save(Arrays.asList("heimat_active", "heimat_url", "heimat_pat"));
   }
}
