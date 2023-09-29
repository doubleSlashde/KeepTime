package de.doubleslash.keeptime.model.repos;

import de.doubleslash.keeptime.model.Authorities;
import de.doubleslash.keeptime.model.User;
import de.doubleslash.keeptime.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuthoritiesRepository extends JpaRepository<Authorities, Long> {
   Authorities findByUserName(String username);
}
