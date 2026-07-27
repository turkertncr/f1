package org.app.f1.repositories;

import org.app.f1.entities.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepo extends JpaRepository<Sector, Long> {

    boolean existsByLapSessionSessionKeyAndLapDriverNumber(int sessionKey, int driverNumber);
}
