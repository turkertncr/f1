package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.dto.EntityMapper;
import org.app.f1.entities.Result;

import java.util.List;

public record ResultRequest(
        @JsonProperty("dnf") Boolean dnf,
        @JsonProperty("dns") Boolean dns,
        @JsonProperty("dsq") Boolean dsq,
        @JsonProperty("driver_number") Integer driverNumber,

        @JsonProperty("duration")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<Double> duration,

        @JsonProperty("gap_to_leader")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<String> gapToLeader,

        @JsonProperty("number_of_laps")
        @JsonAlias("laps")
        Double laps,

        @JsonProperty("meeting_key") Integer meetingKey,
        @JsonProperty("position") Integer position,
        @JsonProperty("session_key") Integer sessionKey
) implements EntityMapper<Result> {

    @Override
    public Result buildEntity() {
        return Result.builder()
                .dnf(dnf)
                .dns(dns)
                .dsq(dsq)
                .driverNumber(driverNumber)
                .duration(duration)
                .gapToLeader(gapToLeader)
                .laps(laps)
                .meetingKey(meetingKey)
                .position(position)
                .sessionKey(sessionKey)
                .build();
    }
}
