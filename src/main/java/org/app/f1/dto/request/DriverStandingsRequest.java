package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DriverStandingsRequest(

        @JsonProperty("meeting_key")
        Integer meetingKey,

        @JsonProperty("session_key")
        Integer sessionKey,

        @JsonProperty("points_current")
        Integer points,

        @JsonProperty("driver_number")
        Integer driverNumber
) {
}
