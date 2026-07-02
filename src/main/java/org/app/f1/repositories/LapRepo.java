package org.app.f1.repositories;

import org.app.f1.entities.Lap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LapRepo extends JpaRepository<Lap, Long> {

    @Query("""
                select distinct l from Lap l
                left join fetch l.sectors
                where l.session.sessionKey = :sessionKey
                and l.driverNumber = :driverNumber
                order by l.lapNumber asc
            """)
    List<Lap> findAllBySessionKeyAndDriverNumber(int sessionKey, int driverNumber);

    @Query("""
                select l from Lap l
                left join fetch l.sectors
                where l.session.sessionKey = :sessionKey
                and l.driverNumber = :driverNumber
                and l.lapNumber = :lapNumber
            """)
    Optional<Lap> findBySessionKeyAndDriverNumberAndLapNumber(int sessionKey, int driverNumber, int lapNumber);

    @Query("""
            select l from Lap l
            where l.session.sessionKey = :sessionKey
            """)
    List<Lap> getAllBySessionKey(int sessionKey);
}
