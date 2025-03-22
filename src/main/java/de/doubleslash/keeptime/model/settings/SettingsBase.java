// Copyright 2025 doubleSlash Net Business GmbH
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

import de.doubleslash.keeptime.model.Setting;
import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import de.doubleslash.keeptime.model.repos.SettingsRepository;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caches settings from database to be used by other Settings classes.
 */
@Service
public class SettingsBase {

   private final SettingsRepository settingsRepository;

   private final Map<String, Setting> settingsMap;

   public SettingsBase(SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;

      settingsMap = settingsRepository.findAll().stream().collect(Collectors.toMap(Setting::getSetting, item -> item));
   }

   public void saveAll() {
      settingsRepository.saveAll(settingsMap.values());
   }

   public void save(List<String> settingsToSave) {
      settingsRepository.saveAll(settingsMap.entrySet().stream().filter(entry-> settingsToSave.contains(entry.getKey())).map(
            Map.Entry::getValue).toList());
   }

   public boolean getBoolean(String key, boolean orDefault) {
      final Setting bySetting = settingsMap.get(key);
      if (bySetting == null)
         return orDefault;
      return Boolean.parseBoolean(bySetting.getSettingValue());
   }

   public void setBoolean(String key, boolean value) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         setting = new Setting(key, "");
      setting.setSettingValue(String.valueOf(value));
      settingsMap.put(key, setting);
   }

   String getString(String key, String orDefault) {
      final Setting bySetting = settingsMap.get(key);
      if (bySetting == null)
         return orDefault;
      return bySetting.getSettingValue();
   }

   public void setString(String key, String value) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         setting = new Setting(key, "");
      setting.setSettingValue(value);
      settingsMap.put(key, setting);
   }

   public Color getColor(String key, Color orDefault) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         return orDefault;
      return new ColorConverter().convertToEntityAttribute(setting.getSettingValue());
   }

   public void setColor(String key, Color value) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         setting = new Setting(key, "");
      setting.setSettingValue(new ColorConverter().convertToDatabaseColumn(value));
      settingsMap.put(key, setting);
   }

   public double getDouble(String key, double orDefault) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         return orDefault;
      return Double.parseDouble(setting.getSettingValue());
   }

   public void setDouble(String key, double value) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         setting = new Setting(key, "");
      setting.setSettingValue(Double.toString(value));
      settingsMap.put(key, setting);
   }

   public int getInt(String key, int orDefault) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         return orDefault;
      return Integer.parseInt(setting.getSettingValue());
   }

   public void setInt(String key, int value) {
      Setting setting = settingsMap.get(key);
      if (setting == null)
         setting = new Setting(key, "");
      setting.setSettingValue(Integer.toString(value));
      settingsMap.put(key, setting);
   }
}
