package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.entities.CarData;
import org.app.f1.entities.Session;

import java.time.Instant;

public record CarDataRequest(
        @JsonProperty("brake") Integer brake,
        @JsonProperty("speed") Integer speed,
        @JsonProperty("n_gear") Integer gear,
        @JsonProperty("throttle") Integer throttle,
        @JsonProperty("drs") Integer drs,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("date") Instant date
) {

    public CarData buildEntity(Session session, int lapNumber) {
        return CarData.builder()
                .brake(brake != null && brake != 0)
                .speed(speed)
                .gear(gear)
                .throttle(throttle)
                .drs(drs)
                .lapNumber(lapNumber)
                .driverNumber(driverNumber)
                .session(session)
                .date(date)
                .build();
    }
}
