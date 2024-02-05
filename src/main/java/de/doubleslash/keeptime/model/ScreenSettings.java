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

package de.doubleslash.keeptime.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ScreenSettings {
   public final ObjectProperty<Boolean> saveWindowPosition = new SimpleObjectProperty<>(false);
   public final ObjectProperty<Double> proportionalX = new SimpleObjectProperty<>(0.5);
   public final ObjectProperty<Double> proportionalY = new SimpleObjectProperty<>(0.5);
   public final ObjectProperty<Integer> screenHash = new SimpleObjectProperty<>(0);
}
