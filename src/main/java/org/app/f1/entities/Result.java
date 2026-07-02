package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "results",
        indexes = @Index(name = "idx_result_session", columnList = "session_id"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_result_session_driver",
                columnNames = {"session_id", "driver_number"}
        )
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dnf")
    private Boolean dnf;

    @Column(name = "dns")
    private Boolean dns;

    @Column(name = "dsq")
    private Boolean dsq;

    @Column(name = "driver_number")
    private Integer driverNumber;

    @Column(name = "duration")
    private List<Double> duration;

    @Column(name = "gap_to_leader")
    private List<String> gapToLeader;

    @Column(name = "laps")
    private Double laps;

    @Column(name = "meeting_key")
    private Integer meetingKey;

    @Column(name = "position")
    private Integer position;

    @Column(name = "session_key")
    private Integer sessionKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Objects.equals(driverNumber, result.getDriverNumber()) &&
                Objects.equals(sessionKey, result.getSessionKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDriverNumber(), getSessionKey());
    }
}
