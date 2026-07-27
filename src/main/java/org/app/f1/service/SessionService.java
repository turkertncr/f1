package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.MeetingResponse;
import org.app.f1.dto.response.SessionResponse;
import org.app.f1.entities.Meeting;
import org.app.f1.entities.Session;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.MeetingRepo;
import org.app.f1.repositories.SessionRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepo sessionRepo;
    private final MeetingRepo meetingRepo;
    private final OpenF1Client openF1Client;
    private final DataImportService dataImportService;
    private final MeetingService meetingService;

    @Cacheable(value = "sessions", key = "#meetingKey")
    public List<Session> findSessions(int meetingKey) {
        List<Session> sessions = sessionRepo.findAllByMeeting_MeetingKey(meetingKey);

        if (sessions.isEmpty()) {
            Meeting meeting = meetingRepo.findByMeetingKey(meetingKey)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Meeting not found with key: " + meetingKey));

            sessions = openF1Client.getSessions(meeting);

            if (!sessions.isEmpty()) {
                dataImportService.saveAllSessions(sessions);
            } else {
                throw new ResourceNotFoundException(
                        "No sessions found for meeting: " + meeting.getName() + " (key: " + meetingKey + ")");
            }
        }
        return sessions;
    }

    @Cacheable(value = "sessions_responses", key = "#meetingKey")
    public List<SessionResponse> getSessionsResponse(int meetingKey) {
        return findSessions(meetingKey).stream().map(SessionResponse::fromEntity).toList();
    }

    public Session fetchSession(int sessionKey) {
        Optional<Session> sessionOptional = sessionRepo.findBySessionKey(sessionKey);
        if (sessionOptional.isEmpty()) {
            var sessionRequest = openF1Client.getSession(sessionKey);

            Meeting meeting = meetingRepo.findByMeetingKey(sessionRequest.meetingKey())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Meeting not found with key: " + sessionRequest.meetingKey()));

            Session session = sessionRequest.buildEntity(meeting);
            sessionRepo.save(session);
            return session;
        }
        return sessionOptional.get();
    }

    public int getLastSessionKeyByYear(int year) {

        int lastMeetingKey = 0;
        var meetings = meetingService.loadAllByYear(year);

        long minDuration = Long.MAX_VALUE;
        for (Meeting meeting : meetings) {
            if (meeting.getDateEnd().isAfter(Instant.now())) {
                continue;
            }

            long now =  System.currentTimeMillis();
            long meetingEnd = meeting.getDateEnd().toEpochMilli();

            long duration = now - meetingEnd;

            if (minDuration > duration) {
                minDuration = duration;
                lastMeetingKey = meeting.getMeetingKey();
            }
        }

        int finalLastMeetingKey = lastMeetingKey;
        Session last = findSessions(lastMeetingKey).stream().max(Comparator.comparing(Session::getDateEnd))
                .orElseThrow(() -> new RuntimeException("No session found for meeting key %d".formatted(finalLastMeetingKey)));

        return last.getSessionKey();
    }
}