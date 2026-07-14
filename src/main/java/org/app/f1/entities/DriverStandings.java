package org.app.f1.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "driver_standings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_key", "driver_number"})
        })
@Data
@NoArgsConstructor
public class DriverStandings extends Standings {

    @JsonProperty("driver_number")
    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    public boolean equals(Object o) {
        if  (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverStandings that = (DriverStandings) o;
        return Objects.equals(driverNumber, that.driverNumber) &&
                Objects.equals(getSessionKey(), that.getSessionKey());
    }

    public int hashCode() {
        return Objects.hash(driverNumber, getSessionKey());
    }
}
