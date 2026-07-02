package org.app.f1.repositories;

import org.app.f1.entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DriverRepo extends JpaRepository<Driver, Long> {

    Optional<Driver> findByNormalizedName(String normalizedName);

}
