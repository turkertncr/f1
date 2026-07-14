package org.app.f1.repositories;

import org.app.f1.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TeamRepo extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);

    default Map<String, Team> getTeamMap() {
        return findAll().stream().collect(Collectors.toMap(Team::getName, t -> t));
    }
}
