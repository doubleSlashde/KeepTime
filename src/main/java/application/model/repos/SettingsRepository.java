package application.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import application.model.Settings;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {

}
