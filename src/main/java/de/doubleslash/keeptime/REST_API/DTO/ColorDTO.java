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

package de.doubleslash.keeptime.REST_API.DTO;

public class ColorDTO {
   private double red;
   private double green;
   private double blue;
   private double opacity;

   public double getRed() {
      return red;
   }

   public void setRed(double red) {
      this.red = red;
   }

   public double getGreen() {
      return green;
   }

   public void setGreen(double green) {
      this.green = green;
   }

   public double getBlue() {
      return blue;
   }

   public void setBlue(double blue) {
      this.blue = blue;
   }

   public double getOpacity() {
      return opacity;
   }

   public void setOpacity(double opacity) {
      this.opacity = opacity;
   }
}
