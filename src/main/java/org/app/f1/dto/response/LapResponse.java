package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Lap;
import org.app.f1.entities.Sector;

import java.time.Instant;
import java.util.List;

@Builder
public record LapResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("session_id")
        Long sessionId,

        @JsonProperty("driver_number")
        Integer driverNumber,

        @JsonProperty("lap_start")
        Instant lapStart,

        @JsonProperty("lap_number")
        Integer lapNumber,

        @JsonProperty("duration")
        Double duration,

        @JsonProperty("is_pit_lap")
        boolean isPitLap,

        @JsonProperty("sectors")
        List<Double> sectors,

        @JsonProperty("outlier")
        boolean isOutlier,

        @JsonProperty("avg_speed")
        Integer avgSpeed,

        @JsonProperty("top_speed")
        Integer topSpeed
) {
    public static LapResponse fromEntity(Lap lap, int avgSpeed, int topSpeed) {
        return LapResponse.builder()
                .id(lap.getId())
                .sessionId(lap.getSession().getId())
                .driverNumber(lap.getDriverNumber())
                .lapStart(lap.getLapStart())
                .lapNumber(lap.getLapNumber())
                .avgSpeed(avgSpeed)
                .topSpeed(topSpeed)
                .duration(lap.getDuration())
                .sectors(lap.getSectors().stream().map(Sector::getTime).toList())
                .isPitLap(lap.isPitLap())
                .isOutlier(lap.isOutlier())
                .build();
    }
}
