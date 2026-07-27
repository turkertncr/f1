package org.app.f1.repositories;

import org.app.f1.entities.Standings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface StandingsRepo<T extends Standings> extends JpaRepository<T, Long> {

    List<T> findBySessionKey(int sessionKey);
}
