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

import jakarta.persistence.*;

@Table(name = "Users")
@Entity
public class User {

   @Id
   @Column(name = "USERNAME")
   private String userName;

   @Column(name = "PASSWORD")
   private String password;
   @Column(name = "ENABLED")
   private boolean enabled;

   public void setUserName(final String userName) {
      this.userName = userName;
   }

   public void setPassword(final String password) {
      this.password = password;
   }

   public void setEnabled(final boolean enabled) {
      this.enabled = enabled;
   }

   public String getUserName() {
      if (userName != null) {
         return userName;
      } else {
         return "No username set";
      }
   }
}
