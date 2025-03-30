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

import de.doubleslash.keeptime.model.settings.SettingsBase;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;

/**
 * Object holding settings
 *
 * @author nmutter
 */
@Service
public class Settings {

  SettingsBase settingsBase;

  public Settings(SettingsBase settingsBase) {
    this.settingsBase = settingsBase;
  }

  // TODO add default values

  public boolean isRemindIfNotesAreEmptyOnlyForWorkEntry() {
    return settingsBase.getBoolean("remind_if_notes_are_empty_only_for_work_entry", false);
  }

  public void setRemindIfNotesAreEmptyOnlyForWorkEntry(boolean emptyNoteReminderCheckBoxIsWork) {
    settingsBase.setBoolean("remind_if_notes_are_empty_only_for_work_entry",
        emptyNoteReminderCheckBoxIsWork);
  }

  public boolean isConfirmClose() {
    return settingsBase.getBoolean("confirm_close", false);
  }

  public void setConfirmClose(boolean confirmClose) {
    settingsBase.setBoolean("confirm_close", confirmClose);
  }

  public Color getHoverBackgroundColor() {
    return settingsBase.getColor("hover_background_color", Model.ORIGINAL_HOVER_BACKGROUND_COLOR);
  }

  public void setHoverBackgroundColor(final Color hoverBackgroundColor) {
    settingsBase.setColor("hover_background_color", hoverBackgroundColor);
  }

  public Color getHoverFontColor() {
    return settingsBase.getColor("hover_font_color", Model.ORIGINAL_HOVER_Font_COLOR);
  }

  public void setHoverFontColor(final Color hoverFontColor) {
    settingsBase.setColor("hover_font_color", hoverFontColor);
  }

  public Color getDefaultBackgroundColor() {
    return settingsBase.getColor("default_background_color",  Model.ORIGINAL_DEFAULT_BACKGROUND_COLOR);
  }

  public void setDefaultBackgroundColor(final Color defaultBackgroundColor) {
    settingsBase.setColor("default_background_color", defaultBackgroundColor);
  }

  public Color getDefaultFontColor() {
    return settingsBase.getColor("default_font_color",  Model.ORIGINAL_DEFAULT_FONT_COLOR);
  }

  public void setDefaultFontColor(final Color defaultFontColor) {
    settingsBase.setColor("default_font_color", defaultFontColor);
  }

  public Color getTaskBarColor() {
    return settingsBase.getColor("task_bar_color",  Model.ORIGINAL_TASK_BAR_FONT_COLOR);
  }

  public void setTaskBarColor(final Color taskBarColor) {
    settingsBase.setColor("task_bar_color", taskBarColor);
  }

  public boolean isUseHotkey() {
    return settingsBase.getBoolean("use_hotkey", false);
  }

  public void setUseHotkey(final boolean useHotkey) {
    settingsBase.setBoolean("use_hotkey", useHotkey);
  }

  public boolean isDisplayProjectsRight() {
    return settingsBase.getBoolean("display_projects_right", false);
  }

  public void setDisplayProjectsRight(final boolean displayProjectsRight) {
    settingsBase.setBoolean("display_projects_right", displayProjectsRight);
  }

  public boolean isHideProjectsOnMouseExit() {
    return settingsBase.getBoolean("hide_projects_on_mouse_exit", false);
  }

  public void setHideProjectsOnMouseExit(final boolean hideProjectsOnMouseExit) {
    settingsBase.setBoolean("hide_projects_on_mouse_exit", hideProjectsOnMouseExit);
  }

  public double getWindowXProportion() {
    return settingsBase.getDouble("windowxproportion", 0.5);
  }

  public void setWindowXProportion(final double windowPositionX) {
    settingsBase.setDouble("windowxproportion", windowPositionX);
  }

  public double getWindowYProportion() {
    return settingsBase.getDouble("windowyproportion", 0.5);
  }

  public void setWindowYProportion(final double windowPositionY) {
    settingsBase.setDouble("windowyproportion", windowPositionY);
  }

  public int getScreenHash() {
    return settingsBase.getInt("window_screenhash", 0);
  }

  public void setScreenHash(final int screenHash) {
    settingsBase.setInt("window_screenhash", screenHash);
  }

  public boolean isSaveWindowPosition() {
    return settingsBase.getBoolean("save_window_position", false);
  }

  public void setSaveWindowPosition(final boolean saveWindowPosition) {
    settingsBase.setBoolean("save_window_position", saveWindowPosition);
  }

  public boolean isRemindIfNotesAreEmpty() {
    return settingsBase.getBoolean("remind_if_notes_are_empty", false);
  }

  public void setRemindIfNotesAreEmpty(final boolean emptyNoteReminder) {
    settingsBase.setBoolean("remind_if_notes_are_empty", emptyNoteReminder);
  }

  public void save() {
    settingsBase.saveAll();
  }
}
