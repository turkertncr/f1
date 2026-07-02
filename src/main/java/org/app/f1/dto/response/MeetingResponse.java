package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Meeting;

@Builder
public record MeetingResponse(
        @JsonProperty("meeting_key")
        Integer meetingKey,

        @JsonProperty("meeting_name")
        String meetingName,

        @JsonProperty("country_name")
        String countryName,

        @JsonProperty("location")
        String location
) {
    public static MeetingResponse fromEntity(Meeting meeting) {
        return MeetingResponse.builder()
                .meetingKey(meeting.getMeetingKey())
                .meetingName(meeting.getName())
                .countryName(meeting.getCountryName())
                .location(meeting.getLocation())
                .build();
    }
}
