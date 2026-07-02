package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.jdbc.Expectation;

import java.util.Objects;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stints",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"stint_number", "session_id", "driver_number"}
        ))
public class Stint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lap_start")
    private Integer lapStart;

    @Column(name = "lap_end")
    private Integer lapEnd;

    @Column(name = "stint_number")
    private Integer stintNumber;

    @Column(name = "tyre_age_at_start")
    private Integer tyreAge;

    @JoinColumn(name = "session_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Session session;

    @JoinColumn(name = "meeting_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Meeting meeting;

    @Column(name = "driver_number")
    private Integer driverNumber;

    @Enumerated(EnumType.STRING)
    private Compound compound;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stint stints = (Stint) o;
        return Objects.equals(stintNumber, stints.stintNumber) && session == stints.session;
    }

    @Override
    public int hashCode() { return Objects.hash(stintNumber, session); }
}
