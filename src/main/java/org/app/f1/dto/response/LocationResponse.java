package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Location;

@Builder
public record LocationResponse(
        @JsonProperty("driver_number") int driverNumber,
        @JsonProperty("x") Integer x,
        @JsonProperty("y") Integer y
) {
    public static LocationResponse fromEntity(Location location) {
        return LocationResponse.builder()
                .driverNumber(location.getDriverNumber())
                .x(location.getX())
                .y(location.getY())
                .build();
    }
}
