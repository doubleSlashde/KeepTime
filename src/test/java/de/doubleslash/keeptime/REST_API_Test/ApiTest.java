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


