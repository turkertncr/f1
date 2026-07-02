package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.entities.RaceControlEvent;
import org.app.f1.entities.Session;

import java.time.Instant;

public record RaceControlEventRequest(
        @JsonProperty("category") String category,
        @JsonProperty("date") Instant date,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("flag") String flag,
        @JsonProperty("lap_number") Integer lapNumber,
        @JsonProperty("meeting_key") Integer meetingKey,
        @JsonProperty("message") String message,
        @JsonProperty("qualifying_phase") String qualifyingPhase,
        @JsonProperty("scope") String scope,
        @JsonProperty("sector") Integer sector,
        @JsonProperty("session_key") Integer sessionKey
) {

    public RaceControlEvent buildEntity(Session session) {
        return RaceControlEvent.builder()
                .session(session)
                .category(category)
                .date(date)
                .driverNumber(driverNumber)
                .flag(flag)
                .lapNumber(lapNumber)
                .message(message)
                .qualifyingPhase(qualifyingPhase)
                .scope(scope)
                .sector(sector)
                .build();
    }
}
