package de.doubleslash.keeptime.model.repos;

import de.doubleslash.keeptime.model.Authorities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthoritiesRepository extends JpaRepository<Authorities, Long> {
   Authorities findByUserName(String username);
}
