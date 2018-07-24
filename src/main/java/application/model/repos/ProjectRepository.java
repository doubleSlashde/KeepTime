package application.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import application.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

}
