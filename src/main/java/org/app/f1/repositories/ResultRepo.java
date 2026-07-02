package org.app.f1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.app.f1.entities.Result;

import java.util.Set;

public interface ResultRepo extends JpaRepository<Result,Long> {

    Set<Result> findAllBySession_SessionKey(int sessionKey);
}
