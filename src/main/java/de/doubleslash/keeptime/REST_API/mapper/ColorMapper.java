// Copyright 2024 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.REST_API.mapper;

import de.doubleslash.keeptime.REST_API.DTO.ColorDTO;
import javafx.scene.paint.Color;
import org.mapstruct.Mapper;

@Mapper
public interface ColorMapper {
   default ColorDTO colorToColorDTO(Color color) {
      if (color == null) {
         return null;
      }

      ColorDTO colorDTO = new ColorDTO();
      colorDTO.setRed(Color.RED.getRed());
      colorDTO.setGreen(color.getGreen());
      colorDTO.setBlue(color.getBlue());
      colorDTO.setOpacity(color.getOpacity());

      return colorDTO;
   }

   default Color colorDTOToColor(ColorDTO colorDTO) {
      if (colorDTO == null) {
         return null;
      }

      return new Color(colorDTO.getRed(), colorDTO.getGreen(), colorDTO.getBlue(), colorDTO.getOpacity());
   }
}
