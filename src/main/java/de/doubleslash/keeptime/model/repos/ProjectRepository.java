// Copyright 2019 doubleSlash Net Business GmbH

package de.doubleslash.keeptime.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.doubleslash.keeptime.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

}
