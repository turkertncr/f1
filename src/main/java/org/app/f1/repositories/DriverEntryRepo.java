package org.app.f1.repositories;

import org.app.f1.entities.Driver;
import org.app.f1.entities.DriverEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverEntryRepo extends JpaRepository<DriverEntry, Long> {

    Optional<DriverEntry> findByDriverAndSeason(Driver driver, int season);

    List<DriverEntry> findByDriverInAndSeason(java.util.Collection<Driver> drivers, int season);

    long countByDriverInAndSeason(java.util.Collection<Driver> drivers, int season);
}

