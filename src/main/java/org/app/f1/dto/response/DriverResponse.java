package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Driver;
import org.app.f1.entities.DriverEntry;

@Builder
public record DriverResponse(
        @JsonProperty("driver_number")
        Integer driverNumber,

        @JsonProperty("broadcast_name")
        String broadcastName,

        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("name_acronym")
        String nameAcronym,

        @JsonProperty("team_name")
        String teamName,

        @JsonProperty("team_colour")
        String teamColour,

        @JsonProperty("headshot_url")
        String headshotUrl,

        @JsonProperty("country_code")
        String countryCode
) {
    public static DriverResponse fromEntity(Driver driver, DriverEntry driverEntry) {
        return builder()
                .driverNumber(driverEntry.getDriverNumber())
                .broadcastName(driver.getBroadcastName())
                .fullName(driver.getFullName())
                .nameAcronym(driverEntry.getAcronym())
                .teamName(driverEntry.getTeam().getName())
                .teamColour(driverEntry.getTeam().getTeamColor())
                .headshotUrl(driver.getHeadshotUrl())
                .countryCode(driver.getCountryCode())
                .build();
    }
}
