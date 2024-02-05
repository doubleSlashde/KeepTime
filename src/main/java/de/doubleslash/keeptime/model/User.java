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
