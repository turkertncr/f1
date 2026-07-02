package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.RaceControlEvent;

import java.time.Instant;

@Builder
public record RaceControlEventResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("session_id") Long sessionId,
        @JsonProperty("category") String category,
        @JsonProperty("date") Instant date,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("flag") String flag,
        @JsonProperty("lap_number") Integer lapNumber,
        @JsonProperty("message") String message,
        @JsonProperty("qualifying_phase") String qualifyingPhase,
        @JsonProperty("scope") String scope,
        @JsonProperty("sector") Integer sector
) {
    public static RaceControlEventResponse fromEntity(RaceControlEvent event) {
        return RaceControlEventResponse.builder()
                .id(event.getId())
                .sessionId(event.getSession().getId())
                .category(event.getCategory())
                .date(event.getDate())
                .driverNumber(event.getDriverNumber())
                .flag(event.getFlag())
                .lapNumber(event.getLapNumber())
                .message(event.getMessage())
                .qualifyingPhase(event.getQualifyingPhase())
                .scope(event.getScope())
                .sector(event.getSector())
                .build();
    }
}
