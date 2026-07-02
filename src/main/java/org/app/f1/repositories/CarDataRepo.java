package org.app.f1.repositories;

import org.app.f1.entities.CarData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CarDataRepo extends JpaRepository<CarData, Long> {

    @Query("""
                select c from CarData c
                where c.session.sessionKey = :sessionKey
                and c.driverNumber = :driverNumber
                and c.date >= :dateStart
                and c.date <= :dateEnd
                order by c.date asc
            """)
    List<CarData> loadCarData(
            @Param("sessionKey") int sessionKey,
            @Param("dateStart") Instant dateStart,
            @Param("dateEnd") Instant dateEnd,
            @Param("driverNumber") int driverNumber);


    @Query("""
                select c from CarData c
                where c.session.sessionKey = :sessionKey
                and c.driverNumber = :driverNumber
                order by c.date asc
""")
    List<CarData> getAllByDriverNumberAndSession(
            @Param("driverNumber") Integer driverNumber,
            @Param("sessionKey") Integer sessionKey);


}
