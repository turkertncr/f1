package org.app.f1.repositories;

import org.app.f1.entities.DriverStandings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverStandingsRepo extends JpaRepository<DriverStandings, Long> {

    List<DriverStandings> findBySessionKey(int sessionKey);
}
