package de.doubleslash.keeptime.common;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class DefaultPasswordEncoder {

   private static Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(16, 32, 4, 128000, 10);

   public static final Argon2PasswordEncoder getPasswordEncoder() {
      return DefaultPasswordEncoder.passwordEncoder;
   }

}
