package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.jdbc.Expectation;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "race_control_event",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"session_id", "date", "category", "driver_number"}
        ),
        indexes = {
                @Index(name = "idx_rce_session", columnList = "session_id"),
                @Index(name = "idx_rce_lap_number", columnList = "lap_number")
        }
)
public class RaceControlEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Instant date;

    private Integer driverNumber;

    private String flag;

    private Integer lapNumber;

    private String message;

    private String qualifyingPhase;

    private String scope;

    private Integer sector;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaceControlEvent that = (RaceControlEvent) o;
        return Objects.equals(session, that.session)
                && Objects.equals(date, that.date)
                && Objects.equals(category, that.category)
                && Objects.equals(driverNumber, that.driverNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, date, category, driverNumber);
    }

    public boolean validLap() {
        return Objects.equals(category, "SafetyCar")
                || "RED".equalsIgnoreCase(flag)
                || "DOUBLE YELLOW".equalsIgnoreCase(flag);
    }

}
