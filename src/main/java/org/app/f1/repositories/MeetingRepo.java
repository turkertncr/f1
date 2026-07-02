package org.app.f1.repositories;

import org.app.f1.entities.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRepo extends JpaRepository<Meeting, Long> {

    List<Meeting> findAllByYear(int year);

    Optional<Meeting> findByMeetingKey(int meetingKey);
}
