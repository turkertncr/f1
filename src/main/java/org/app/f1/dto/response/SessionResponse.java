package org.app.f1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.app.f1.entities.Session;


@Builder
public record SessionResponse(
        @JsonProperty("session_key")
        Integer sessionKey,

        @JsonProperty("session_name")
        String sessionName,

        @JsonProperty("session_type")
        String sessionType
) {
    public static SessionResponse fromEntity(Session session) {
        return SessionResponse.builder()
                .sessionKey(session.getSessionKey())
                .sessionName(session.getSessionName())
                .sessionType(session.getType().name())
                .build();
    }
}



