package org.app.f1.repositories;

import org.app.f1.entities.Stint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StintRepo extends JpaRepository<Stint, Long> {

    Optional<List<Stint>> findStintsByDriverNumberAndSessionSessionKeyOrderByStintNumberAsc(int driverNumber, int sessionKey);

}
