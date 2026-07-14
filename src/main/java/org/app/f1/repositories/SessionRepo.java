package org.app.f1.repositories;

import org.app.f1.entities.Session;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepo extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s JOIN FETCH s.meeting WHERE s.meeting.meetingKey = :meetingKey")
    List<Session> findAllByMeeting_MeetingKey(@Param("meetingKey") int meetingKey);

    @EntityGraph(attributePaths = {"meeting"})
    Optional<Session> findBySessionKey(int sessionKey);

    Optional<Session>
    findFirstByMeeting_YearAndDateEndLessThanEqualOrderByDateEndDesc(int year, Instant now);

    default Optional<Session> findLastSessionKeyByYear(int year) {
        return findFirstByMeeting_YearAndDateEndLessThanEqualOrderByDateEndDesc(year, Instant.now());
    }
}
