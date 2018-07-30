package application.model.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import application.model.Work;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {

   List<Work> findByCreationDate(LocalDate creationDate);
}
