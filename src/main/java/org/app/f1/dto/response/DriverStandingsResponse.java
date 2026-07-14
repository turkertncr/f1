package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Driver;
import org.app.f1.entities.DriverStandings;

@Builder
public record DriverStandingsResponse(
        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("driver_number")
        Integer driverNumber,

        @JsonProperty("points")
        Double points
) {
    public static DriverStandingsResponse fromEntity(DriverStandings standings, Driver driver) {
        return builder()
                .fullName(driver != null ? driver.getFullName() : null)
                .driverNumber(standings.getDriverNumber())
                .points(standings.getCurrentPoints())
                .build();
    }
}
