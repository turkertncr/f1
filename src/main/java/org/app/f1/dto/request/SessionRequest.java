package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.dto.EntityMapper;
import org.app.f1.entities.Meeting;
import org.app.f1.entities.Session;
import org.app.f1.entities.SessionType;

import java.time.Instant;

public record SessionRequest(
        @JsonProperty("session_key") int sessionKey,
        @JsonProperty("meeting_key") int meetingKey,
        @JsonProperty("session_name") String sessionName,
        @JsonProperty("session_type") String sessionType,
        @JsonProperty("date_start") Instant dateStart,
        @JsonProperty("date_end") Instant dateEnd
) implements EntityMapper<Session> {

    @Override
    public Session buildEntity() {

        SessionType type;
        try {
           type = SessionType.valueOf(sessionType);
        } catch (Exception e) {
            throw new RuntimeException("Invalid session type");
        }

        return Session.builder()
                .sessionKey(sessionKey)
                .sessionName(sessionName)
                .type(type)
                .dateStart(dateStart)
                .dateEnd(dateEnd)
                .build();
    }

    public Session buildEntity(Meeting meeting) {
        Session session = buildEntity();
        session.setMeeting(meeting);
        return session;
    }
}
