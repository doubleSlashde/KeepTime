package de.ds.keeptime.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.ds.keeptime.model.Settings;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {

}
