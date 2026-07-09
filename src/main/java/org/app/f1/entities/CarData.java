package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(
        name = "car_data",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"driver_number", "session_id", "date"})
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean brake;

    private Integer speed;

    private Integer gear;

    private Integer throttle;

    private Integer drs;

    @Column(name = "driver_number")
    private Integer driverNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id",  nullable = false)
    private Session session;

    @Column(name = "date")
    private Instant date;

    private Integer lapNumber;
}
