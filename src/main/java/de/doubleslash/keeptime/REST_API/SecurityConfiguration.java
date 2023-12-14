// Copyright 2023 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.REST_API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
   @Autowired
   private DataSource dataSource;

   @Autowired
   public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
      auth.jdbcAuthentication().dataSource(dataSource);
   }

   @Bean
   public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
      http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeRequests().anyRequest().authenticated().and().httpBasic().and().csrf().disable();
      return http.build();
   }

   @Bean
   public UserDetailsManager users(@Autowired final AuthenticationManagerBuilder auth) throws Exception {
      final JdbcUserDetailsManager jdbcUserDetailsManager= new JdbcUserDetailsManager(dataSource);
      jdbcUserDetailsManager.setAuthenticationManager(auth.getOrBuild());
      return jdbcUserDetailsManager;
   }
}
