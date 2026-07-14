package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TeamStandingsRequest(

        @JsonProperty("meeting_key")
        Integer meetingKey,

        @JsonProperty("session_key")
        Integer sessionKey,

        @JsonProperty("points_current")
        Integer points,

        @JsonProperty("team_name")
        String teamName
) {
}
