package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.app.f1.dto.EntityMapper;
import org.app.f1.entities.Driver;
import org.app.f1.entities.Team;

public record DriverRequest(
        @JsonProperty("broadcast_name") String broadcastName,
        @JsonProperty("country_code") String countryCode,
        @JsonProperty("driver_number") int driverNumber,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("headshot_url") String headshotUrl,
        @JsonProperty("last_name") String lastName,
        @JsonProperty("name_acronym") String nameAcronym,
        @JsonProperty("team_colour") String teamColour,
        @JsonProperty("team_name") String teamName
) implements EntityMapper<Driver> {

    @Override
    public Driver buildEntity() {
        return Driver.builder()
                .broadcastName(broadcastName)
                .countryCode(countryCode)
                .firstName(firstName)
                .fullName(fullName)
                .headshotUrl(headshotUrl)
                .lastName(lastName)
                .normalizedName(Driver.normalize(fullName()))
                .build();
    }

    public Team buildTeamEntity() {
        Team team = new Team();
        team.setName(teamName);
        team.setTeamColor(teamColour);
        return team;
    }
}
