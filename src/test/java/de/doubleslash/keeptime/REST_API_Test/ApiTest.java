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


package de.doubleslash.keeptime.REST_API_Test;

import de.doubleslash.keeptime.model.Authorities;
import de.doubleslash.keeptime.model.User;
import de.doubleslash.keeptime.model.repos.AuthoritiesRepository;
import de.doubleslash.keeptime.model.repos.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ApiTest {

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private AuthoritiesRepository authoritiesRepository;

   @Test
   public void testSaveUserAndAuthorities() {
      String username = "user";
      String password = "1234";

      User user = new User();
      Authorities authorities = new Authorities();
      user.setUserName(username);
      user.setPassword("{noop}" + password);
      user.setEnabled(true);
      authorities.setUserName(username);
      authorities.setAuthority("ROLE_USER");

      userRepository.save(user);
      authoritiesRepository.save(authorities);

      User savedUser = userRepository.findByUserName(username);
      assertNotNull(savedUser);
      assertEquals(username, savedUser.getUserName());

      Authorities savedAuthorities = authoritiesRepository.findByUserName(username);
      assertNotNull(savedAuthorities);
      assertEquals("ROLE_USER", savedAuthorities.getAuthority());
   }
}


