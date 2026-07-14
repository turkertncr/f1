package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Team;
import org.app.f1.entities.TeamStandings;

@Builder
public record TeamStandingsResponse(
        @JsonProperty("team_name")
        String teamName,

        @JsonProperty("points")
        Double points,

        @JsonProperty("team_colour")
        String teamColour
) {
    public static TeamStandingsResponse fromEntity(TeamStandings standings, Team team) {
        return builder()
                .teamName(standings.getTeamName())
                .points(standings.getCurrentPoints())
                .teamColour(team != null ? team.getTeamColor() : null)
                .build();
    }
}
