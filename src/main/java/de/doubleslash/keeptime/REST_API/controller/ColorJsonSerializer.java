package de.doubleslash.keeptime.REST_API.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorJsonSerializer extends JsonSerializer<Color> {

   @Override
   public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("red", color.getRed());
      jsonGenerator.writeNumberField("green", color.getGreen());
      jsonGenerator.writeNumberField("blue", color.getBlue());
      jsonGenerator.writeNumberField("opacity", color.getOpacity());
      jsonGenerator.writeBooleanField("opaque", color.isOpaque());
      jsonGenerator.writeNumberField("saturation", color.getSaturation());
      jsonGenerator.writeNumberField("brightness", color.getBrightness());
      jsonGenerator.writeNumberField("hue", color.getHue());
      jsonGenerator.writeEndObject();
   }
}