package de.doubleslash.keeptime.model.settings;

import de.doubleslash.keeptime.model.Setting;
import de.doubleslash.keeptime.model.repos.SettingsRepository;
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

   boolean getBoolean(String key, boolean orDefault) {
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
   }
}
