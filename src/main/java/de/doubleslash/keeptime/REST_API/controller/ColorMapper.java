package de.doubleslash.keeptime.REST_API.controller;

import javafx.scene.paint.Color;
import org.mapstruct.Mapper;

@Mapper
public interface ColorMapper {
   default ColorDTO colorToColorDTO(Color color) {

      if (color == null) {
         return null;
      }
      System.out.println(color.getRed());
      System.out.println(color.getGreen());
      System.out.println(color.getBlue());

      System.out.println("-------------------------------");

      ColorDTO colorDTO = new ColorDTO();
      colorDTO.setRed(Color.RED.getRed());
      colorDTO.setGreen(color.getGreen());
      colorDTO.setBlue(color.getBlue());
      colorDTO.setOpacity(color.getOpacity());
      System.out.println(colorDTO.getRed());
      System.out.println(colorDTO.getGreen());
      System.out.println(colorDTO.getBlue());
      return colorDTO;
   }

   default Color colorDTOToColor(ColorDTO colorDTO) {
      if (colorDTO == null) {
         return null;
      }

      return new Color(colorDTO.getRed(), colorDTO.getGreen(), colorDTO.getBlue(), colorDTO.getOpacity());
   }
}
