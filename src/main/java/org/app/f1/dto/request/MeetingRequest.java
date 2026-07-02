package org.app.f1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.app.f1.dto.EntityMapper;
import org.app.f1.entities.Meeting;

import java.time.Instant;

public record MeetingRequest(
        @JsonProperty("meeting_key") int meetingKey,
        @JsonProperty("meeting_name") String meetingName,
        @JsonProperty("meeting_official_name") String officialName,
        @JsonProperty("location") String location,
        @JsonProperty("country_key") Integer countryKey,
        @JsonProperty("country_code") String countryCode,
        @JsonProperty("country_name") String countryName,
        @JsonProperty("circuit_key") int circuitKey,
        @JsonProperty("circuit_short_name") String circuitName,
        @JsonProperty("date_start") Instant dateStart,
        @JsonProperty("year") int year
) implements EntityMapper<Meeting> {

    @Override
    public Meeting buildEntity() {
        return Meeting.builder()
                .meetingKey(meetingKey)
                .name(meetingName)
                .officialName(officialName)
                .location(location)
                .countryName(countryName)
                .countryCode(countryCode)
                .circuitKey(circuitKey)
                .circuitName(circuitName)
                .dateStart(dateStart)
                .year(year)
                .build();
    }
}