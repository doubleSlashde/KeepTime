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

package de.doubleslash.keeptime.model;

import de.doubleslash.keeptime.model.repos.SettingsRepository;

import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;

/**
 * Object holding settings
 *
 * @author nmutter
 */
@Service
public class Settings {

   SettingsRepository settingsRepository;

   public Settings(SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;
   }

   public boolean isRemindIfNotesAreEmptyOnlyForWorkEntry() {
      return getBoolean("remind_if_notes_are_empty_only_for_work_entry");
   }

   public void setRemindIfNotesAreEmptyOnlyForWorkEntry(boolean emptyNoteReminderCheckBoxIsWork) {
      setBoolean( "remind_if_notes_are_empty_only_for_work_entry", emptyNoteReminderCheckBoxIsWork);
   }

   public boolean isConfirmClose() {
      return getBoolean("confirm_close");
   }

   public void setConfirmClose(boolean confirmClose) {
      setBoolean("confirm_close", confirmClose);
   }

   public Color getHoverBackgroundColor() {
      return getColor("hover_background_color");
   }

   public void setHoverBackgroundColor(final Color hoverBackgroundColor) {
      setColor("hover_background_color", hoverBackgroundColor);
   }

   public Color getHoverFontColor() {
      return getColor("hover_font_color");
   }

   public void setHoverFontColor(final Color hoverFontColor) {
      setColor("hover_font_color", hoverFontColor);
   }

   public Color getDefaultBackgroundColor() {
      return getColor("default_background_color");
   }

   public void setDefaultBackgroundColor(final Color defaultBackgroundColor) {
      setColor("default_background_color",defaultBackgroundColor);
   }

   public Color getDefaultFontColor() {
      return getColor("default_font_color");
   }

   public void setDefaultFontColor(final Color defaultFontColor) {
      setColor("default_font_color",defaultFontColor);
   }

   public Color getTaskBarColor() {
      return getColor("task_bar_color");
   }

   public void setTaskBarColor(final Color taskBarColor) {
      setColor("task_bar_color",taskBarColor);
   }

   public boolean isUseHotkey() {
      return getBoolean("use_hotkey");
   }

   public void setUseHotkey(final boolean useHotkey) {
      setBoolean("use_hotkey", useHotkey);
   }

   public boolean isDisplayProjectsRight() {
      return getBoolean("display_projects_right");
   }

   public void setDisplayProjectsRight(final boolean displayProjectsRight) {
      setBoolean("display_projects_right", displayProjectsRight);
   }

   public boolean isHideProjectsOnMouseExit() {
      return getBoolean("hide_projects_on_mouse_exit");
   }

   public void setHideProjectsOnMouseExit(final boolean hideProjectsOnMouseExit) {
      setBoolean("hide_projects_on_mouse_exit",hideProjectsOnMouseExit);
   }

   public double getWindowXProportion() {
      return getDouble("windowxproportion");
   }

   public void setWindowXProportion(final double windowPositionX) {
      setDouble("windowxproportion", windowPositionX);
   }

   public double getWindowYProportion() {
      return getDouble("windowyproportion");
   }

   public void setWindowYProportion(final double windowPositionY) {
      setDouble("windowyproportion", windowPositionY);
   }

   public int getScreenHash() {
      return getInt("window_screenhash");
   }

   public void setScreenHash(final int screenHash) {
      setInt("window_screenhash", screenHash);
   }

   public boolean isSaveWindowPosition() {
      return getBoolean("save_window_position");
   }

   public void setSaveWindowPosition(final boolean saveWindowPosition) {
      setBoolean("save_window_position", saveWindowPosition);
   }

   public boolean isRemindIfNotesAreEmpty() {
      return getBoolean("remind_if_notes_are_empty");
   }

   public void setRemindIfNotesAreEmpty(final boolean emptyNoteReminder) {
      setBoolean("remind_if_notes_are_empty", emptyNoteReminder);
   }

   private boolean getBoolean(String key) {
      return Boolean.getBoolean(settingsRepository.findBySetting(key).getSettingValue());
   }

   public void setBoolean(String key, boolean value) {
      final Setting setting = settingsRepository.findBySetting(key);
      setting.setSettingValue(String.valueOf(value));
      settingsRepository.save(setting);
   }

   private Color getColor(String key) {
      return new ColorConverter().convertToEntityAttribute(settingsRepository.findBySetting(key).getSettingValue());
   }

   public void setColor(String key, Color value) {
      final Setting setting = settingsRepository.findBySetting(key);
      setting.setSettingValue(new ColorConverter().convertToDatabaseColumn(value));
      settingsRepository.save(setting);
   }

   private double getDouble(String key) {
      return Double.parseDouble(settingsRepository.findBySetting(key).getSettingValue());
   }

   public void setDouble(String key, double value) {
      final Setting setting = settingsRepository.findBySetting(key);
      setting.setSettingValue(Double.toString(value));
      settingsRepository.save(setting);
   }

   private int getInt(String key) {
      return Integer.parseInt(settingsRepository.findBySetting(key).getSettingValue());
   }

   public void setInt(String key, int value) {
      final Setting setting = settingsRepository.findBySetting(key);
      setting.setSettingValue(Integer.toString(value));
      settingsRepository.save(setting);
   }
}
