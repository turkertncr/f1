package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.entities.Lap;
import org.app.f1.entities.Session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record LapRequest(
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("session_key") Integer sessionKey,
        @JsonProperty("meeting_key") Integer meetingKey,
        @JsonProperty("duration_sector_1") Double sectorDuration1,
        @JsonProperty("duration_sector_2") Double sectorDuration2,
        @JsonProperty("duration_sector_3") Double sectorDuration3,
        @JsonProperty("segments_sector_1") List<Integer> sectorSegments1,
        @JsonProperty("segments_sector_2") List<Integer> sectorSegments2,
        @JsonProperty("segments_sector_3") List<Integer> sectorSegments3,
        @JsonProperty("lap_duration") Double duration,
        @JsonProperty("date_start") Instant lapStart,
        @JsonProperty("is_pit_out_lap") Boolean isPitLap,
        @JsonProperty("lap_number") Integer lapNumber
) {

    public Lap buildEntity(Session session, boolean isOutlier) {
        return Lap.builder()
                .session(session)
                .driverNumber(driverNumber)
                .lapStart(lapStart)
                .lapNumber(lapNumber)
                .duration(duration)
                .isPitLap(isPitLap != null && isPitLap)
                .outlier(isOutlier)
                .build();
    }
}
