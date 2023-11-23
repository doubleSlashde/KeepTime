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
