package org.app.f1.entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Standings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("meeting_key")
    private Integer meetingKey;

    @JsonProperty("session_key")
    @Column(name = "session_key")
    private Integer sessionKey;

    @JsonProperty("position_start")
    private Integer startPosition;

    @JsonProperty("position_current")
    private Integer currentPosition;

    @JsonProperty("points_start")
    private Double startPoints;

    @JsonProperty("points_current")
    private Double currentPoints;
}

