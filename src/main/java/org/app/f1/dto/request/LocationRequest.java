package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.entities.Location;
import org.app.f1.entities.Session;

import java.time.Instant;

public record LocationRequest(
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("date") Instant date,
        @JsonProperty("x") Integer x,
        @JsonProperty("y") Integer y
) {

    public Location buildEntity(Session session) {
        return Location.builder()
                .driverNumber(driverNumber)
                .date(date)
                .x(x)
                .y(y)
                .session(session)
                .build();
    }
}
