package org.app.f1.repositories;

import org.app.f1.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepo extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
}
