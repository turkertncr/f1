package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import org.app.f1.dto.EntityMapper;
import org.app.f1.entities.Compound;
import org.app.f1.entities.Meeting;
import org.app.f1.entities.Session;
import org.app.f1.entities.Stint;

@Builder
public record StintRequest(
        @JsonProperty("compound") @Enumerated(EnumType.STRING) Compound compound,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("lap_end") Integer lapEnd,
        @JsonProperty("lap_start") Integer lapStart,
        @JsonProperty("meeting_key") Integer meetingKey,
        @JsonProperty("session_key") Integer sessionKey,
        @JsonProperty("stint_number") Integer stintNumber,
        @JsonProperty("tyre_age_at_start") Integer tyreAge
) implements EntityMapper<Stint> {

    public Stint buildEntity(Meeting meeting, Session session) {
        Stint stint = buildEntity();
        stint.setMeeting(meeting);
        stint.setSession(session);
        return stint;
    }

    @Override
    public Stint buildEntity() {
        Compound c = compound == null ? Compound.UNKNOWN : compound;
        return Stint.builder()
                .compound(c)
                .driverNumber(driverNumber)
                .lapEnd(lapEnd)
                .lapStart(lapStart)
                .tyreAge(tyreAge)
                .stintNumber(stintNumber)
                .build();
    }
}
