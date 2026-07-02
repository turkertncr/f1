package org.app.f1.repositories;

import org.app.f1.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface LocationRepo extends JpaRepository<Location, Long> {

    @Query("""
                select l from Location l
                where l.session.sessionKey = :sessionKey
                and l.driverNumber = :driverNumber
                and l.date >= :dateStart
                and l.date <= :dateEnd
            """)
    List<Location> loadLocations(
            int sessionKey, int driverNumber, Instant dateStart, Instant dateEnd
    );

}
