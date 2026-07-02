package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data
@Table(name = "driver_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"driver_id", "season"})
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season")
    private int season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",  nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "driver_number")
    private Integer driverNumber;

    @Column(name = "acronym", length = 3)
    private String acronym;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverEntry driverEntry = (DriverEntry) o;
        return season == driverEntry.season && driver.equals(driverEntry.driver);
    }

    @Override
    public int hashCode() { return Objects.hash(season, driver); }
}
