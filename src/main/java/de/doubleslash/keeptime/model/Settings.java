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

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import javafx.scene.paint.Color;

/**
 * Object holding settings
 * 
 * @author nmutter
 */
@Entity
@Table(name = "Settings")
public class Settings {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color hoverBackgroundColor;
   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color hoverFontColor;
   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color defaultBackgroundColor;
   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color defaultFontColor;

   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color taskBarColor;

   private boolean useHotkey;

   private boolean displayProjectsRight;

   private boolean hideProjectsOnMouseExit;

   private double windowXProportion;

   private double windowYProportion;

   private int windowScreenhash;

   private boolean saveWindowPosition;

   private boolean remindIfNotesAreEmpty;

   private boolean remindIfNotesAreEmptyOnlyForWorkEntry;

   private boolean confirmClose;

   public Settings() {}

   public Settings(final Color hoverBackgroundColor, final Color hoverFontColor, final Color defaultBackgroundColor,
         final Color defaultFontColor, final Color taskBarColor, final boolean useHotkey,
         final boolean displayProjectsRight, final boolean hideProjectsOnMouseExit, final double windowPositionX,
         final double windowPositionY, final int screenHash, final boolean saveWindowPosition,
         final boolean remindIfNotesAreEmpty, final boolean remindIfNotesAreEmptyOnlyForWorkEntry,
         final boolean confirmClose) {
      this.hoverBackgroundColor = hoverBackgroundColor;
      this.hoverFontColor = hoverFontColor;
      this.defaultBackgroundColor = defaultBackgroundColor;
      this.defaultFontColor = defaultFontColor;
      this.taskBarColor = taskBarColor;
      this.useHotkey = useHotkey;
      this.displayProjectsRight = displayProjectsRight;
      this.hideProjectsOnMouseExit = hideProjectsOnMouseExit;
      this.windowXProportion = windowPositionX;
      this.windowYProportion = windowPositionY;
      this.windowScreenhash = screenHash;
      this.saveWindowPosition = saveWindowPosition;
      this.remindIfNotesAreEmpty = remindIfNotesAreEmpty;
      this.remindIfNotesAreEmptyOnlyForWorkEntry = remindIfNotesAreEmptyOnlyForWorkEntry;
      this.confirmClose = confirmClose;

   }

   public boolean isRemindIfNotesAreEmptyOnlyForWorkEntry() {
      return remindIfNotesAreEmptyOnlyForWorkEntry;
   }

   public void setRemindIfNotesAreEmptyOnlyForWorkEntry(boolean emptyNoteReminderCheckBoxIsWork) {
      this.remindIfNotesAreEmptyOnlyForWorkEntry = emptyNoteReminderCheckBoxIsWork;
   }

   public boolean isConfirmClose() {
      return confirmClose;
   }

   public void setConfirmClose(boolean confirmClose) {
      this.confirmClose = confirmClose;
   }

   public long getId() {
      return id;
   }

   public Color getHoverBackgroundColor() {
      return hoverBackgroundColor;
   }

   public void setHoverBackgroundColor(final Color hoverBackgroundColor) {
      this.hoverBackgroundColor = hoverBackgroundColor;
   }

   public Color getHoverFontColor() {
      return hoverFontColor;
   }

   public void setHoverFontColor(final Color hoverFontColor) {
      this.hoverFontColor = hoverFontColor;
   }

   public Color getDefaultBackgroundColor() {
      return defaultBackgroundColor;
   }

   public void setDefaultBackgroundColor(final Color defaultBackgroundColor) {
      this.defaultBackgroundColor = defaultBackgroundColor;
   }

   public Color getDefaultFontColor() {
      return defaultFontColor;
   }

   public void setDefaultFontColor(final Color defaultFontColor) {
      this.defaultFontColor = defaultFontColor;
   }

   public Color getTaskBarColor() {
      return taskBarColor;
   }

   public void setTaskBarColor(final Color taskBarColor) {
      this.taskBarColor = taskBarColor;
   }

   public boolean isUseHotkey() {
      return useHotkey;
   }

   public void setUseHotkey(final boolean useHotkey) {
      this.useHotkey = useHotkey;
   }

   public boolean isDisplayProjectsRight() {
      return displayProjectsRight;
   }

   public void setDisplayProjectsRight(final boolean displayProjectsRight) {
      this.displayProjectsRight = displayProjectsRight;
   }

   public boolean isHideProjectsOnMouseExit() {
      return hideProjectsOnMouseExit;
   }

   public void setHideProjectsOnMouseExit(final boolean hideProjectsOnMouseExit) {
      this.hideProjectsOnMouseExit = hideProjectsOnMouseExit;
   }

   public double getWindowXProportion() {
      return windowXProportion;
   }

   public void setWindowXProportion(final double windowPositionX) {
      this.windowXProportion = windowPositionX;
   }

   public double getWindowYProportion() {
      return windowYProportion;
   }

   public void setWindowYProportion(final double windowPositionY) {
      this.windowYProportion = windowPositionY;
   }

   public int getScreenHash() {
      return windowScreenhash;
   }

   public void setScreenHash(final int screenHash) {
      this.windowScreenhash = screenHash;
   }

   public boolean isSaveWindowPosition() {
      return saveWindowPosition;
   }

   public void setSaveWindowPosition(final boolean saveWindowPosition) {
      this.saveWindowPosition = saveWindowPosition;
   }

   public boolean isRemindIfNotesAreEmpty() {
      return remindIfNotesAreEmpty;
   }

   public void setRemindIfNotesAreEmpty(final boolean emptyNoteReminder) {
      this.remindIfNotesAreEmpty = emptyNoteReminder;
   }

}
