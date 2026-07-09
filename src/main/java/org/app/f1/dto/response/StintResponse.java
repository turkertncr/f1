package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Compound;
import org.app.f1.entities.Stint;

@Builder
public record StintResponse(
        @JsonProperty("stint_number")
        Integer stintNumber,

        @JsonProperty("lap_start")
        Integer lapStart,

        @JsonProperty("lap_end")
        Integer lapEnd,

        @JsonProperty("compound")
        String compound,

        @JsonProperty("tyre_age")
        Integer tyreAge,

        @JsonProperty("driver_number")
        Integer driverNumber
) {
    public static StintResponse fromEntity(Stint stint) {
        return builder()
                .stintNumber(stint.getStintNumber())
                .lapStart(stint.getLapStart())
                .lapEnd(stint.getLapEnd())
                .compound(stint.getCompound() != null ? stint.getCompound().name() : Compound.UNKNOWN.name())
                .tyreAge(stint.getTyreAge())
                .driverNumber(stint.getDriverNumber())
                .build();
    }
}

