package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Result;

import java.util.List;

@Builder
public record ResultResponse(
        @JsonProperty("dnf") Boolean dnf,
        @JsonProperty("dns") Boolean dns,
        @JsonProperty("dsq") Boolean dsq,
        @JsonProperty("driver_number") Integer driverNumber,
        @JsonProperty("duration") List<Double> duration,
        @JsonProperty("gap_to_leader") List<String> gapToLeader,
        @JsonProperty("laps") Double laps,
        @JsonProperty("position") Integer position,
        @JsonProperty("driver") DriverResponse driver
) {
    public static ResultResponse fromEntity(Result result, DriverResponse driver) {
        return ResultResponse.builder()
                .dnf(result.getDnf())
                .dns(result.getDns())
                .dsq(result.getDsq())
                .driverNumber(result.getDriverNumber())
                .duration(result.getDuration())
                .gapToLeader(result.getGapToLeader())
                .laps(result.getLaps())
                .driver(driver)
                .position(result.getPosition())
                .laps(result.getLaps())
                .position(result.getPosition())
                .build();
    }
}
