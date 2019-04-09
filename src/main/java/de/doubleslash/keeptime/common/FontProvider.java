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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.common.Resources.RESOURCE;
import javafx.scene.text.Font;

public class FontProvider {

   private static final Logger LOG = LoggerFactory.getLogger(FontProvider.class);

   private static Font defaultFont;
   private static Font boldFont;

   private FontProvider() {
      // no instances allowed
   }

   public static Font getDefaultFont() {
      return defaultFont;
   }

   public static Font getBoldFont() {
      return boldFont;
   }

   public static void loadFonts() {
      LOG.info("Loading fonts");
      defaultFont = loadFont(RESOURCE.FONT_REGULAR);
      boldFont = loadFont(RESOURCE.FONT_BOLD);
   }

   private static Font loadFont(final RESOURCE fontResource) {
      LOG.info("Loading font '{}'", fontResource);
      final Font font = Font.loadFont(Resources.getResource(fontResource).toExternalForm(), 12);
      LOG.info("Font with name '{}' loaded.", font.getName());
      return font;
   }
}
