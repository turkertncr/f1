package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.CarData;

import java.time.Instant;

@Builder
public record CarDataResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("session_id") Long sessionId,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("date") Instant date,
        @JsonProperty("brake") Boolean brake,
        @JsonProperty("speed") Integer speed,
        @JsonProperty("gear") Integer gear,
        @JsonProperty("throttle") Integer throttle,
        @JsonProperty("drs") Integer drs
) {
    public static CarDataResponse fromEntity(CarData carData) {
        return CarDataResponse.builder()
                .id(carData.getId())
                .sessionId(carData.getSession().getId())
                .driverNumber(carData.getDriverNumber())
                .date(carData.getDate())
                .brake(carData.getBrake())
                .speed(carData.getSpeed())
                .gear(carData.getGear())
                .throttle(carData.getThrottle())
                .drs(carData.getDrs())
                .build();
    }
}
