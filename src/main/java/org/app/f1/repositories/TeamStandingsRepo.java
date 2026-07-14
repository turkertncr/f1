package org.app.f1.repositories;

import org.app.f1.entities.TeamStandings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamStandingsRepo extends JpaRepository<TeamStandings, Long> {

    List<TeamStandings> findBySessionKey(int sessionKey);
}
