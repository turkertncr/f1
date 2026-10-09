package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "lap",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"session_id", "driver_number", "lap_number"}
        ),
        indexes = {
                @Index(name = "idx_lap_session", columnList = "session_id"),
                @Index(name = "idx_lap_driver_number", columnList = "driver_number")
        }
)
public class Lap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id",  nullable = false)
    private Session session;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    @OneToMany(mappedBy = "lap", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @OrderBy("sector asc")
    @Builder.Default
    private List<Sector> sectors = new ArrayList<>();

    private Instant lapStart;

    private Integer lapNumber;

    private Double duration;

    private boolean isPitLap;

    private boolean outlier;

    public Optional<TimeWindow> timeWindow() {
        if (lapStart == null || duration == null) {
            return Optional.empty();
        }
        return Optional.of(new TimeWindow(lapStart, lapStart.plusMillis((long) (duration * 1001))));
    }

    public record TimeWindow(Instant start, Instant end) {
    }
}
