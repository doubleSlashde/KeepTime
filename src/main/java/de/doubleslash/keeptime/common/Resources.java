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

package de.doubleslash.keeptime.common;

import java.net.URL;

public class Resources {

   private Resources() {
      throw new IllegalStateException("Utility class");
   }

   public enum RESOURCE {
      /** FONTS **/
      FONT_BOLD("/font/OpenSans-Bold.ttf"),
      FONT_SEMI_BOLD("/font/OpenSans-SemiBold.ttf"),
      FONT_REGULAR("/font/OpenSans-Regular.ttf"),

      /** LAYOUTS **/
      // main
      FXML_VIEW_LAYOUT("/layouts/ViewLayout.fxml"),
      FXML_PROJECT_LAYOUT("/layouts/ProjectDetailLayout.fxml"),
      FXML_SETTINGS("/layouts/settings.fxml"),
      FXML_VIEW_POPUP_LAYOUT("/layouts/ViewLayoutPopup.fxml"),
      FXML_REPORT("/layouts/report.fxml"),

      FXML_MANAGE_PROJECT("/layouts/manage-project.fxml"),
      FXML_MANAGE_WORK("/layouts/manage-work.fxml"),

      SVG_CALENDAR_DAYS_ICON("/svgs/calendar-days.svg"),

      SVG_CLOSE_ICON("/svgs/xmark.svg"),

      SVG_SETTINGS_ICON("/svgs/gear.svg"),

      SVG_MINUS_ICON("/svgs/minus.svg"),

      SVG_TRASH_ICON("/svgs/trash-can.svg"),

      SVG_PENCIL_ICON("/svgs/pencil.svg"),

      SVG_CLIPBOARD("/svgs/clipboard.svg"),

      SVG_BUG_ICON("/svgs/bug.svg"),

      SVG_LAYOUT_ICON("/svgs/border-all.svg"),

      SVG_ABOUT_ICON("/svgs/info.svg"),

      SVG_COLOR_ICON("/svgs/palette.svg"),

      SVG_IMPORT_EXPORT_ICON("/svgs/sort.svg"),

      SVG_LICENSES_ICON("/svgs/closed-captioning.svg"),

      SVG_RANDOM_COLOR_BUTTON("/svgs/dice.svg"),

      ICON_MAIN("/icons/icon.png"),

      ;

      String resourceLocation;

      private RESOURCE(final String resourceLocation) {
         this.resourceLocation = resourceLocation;
      }

      public String getResourceLocation() {
         return resourceLocation;
      }
   }

   public static URL getResource(final RESOURCE resource) {
      return Resources.class.getResource(resource.getResourceLocation());
   }
}
