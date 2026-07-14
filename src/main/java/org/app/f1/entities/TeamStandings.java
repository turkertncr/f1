package org.app.f1.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "team_standings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_key", "team_name"})
})
@Data
@NoArgsConstructor
public class TeamStandings extends Standings {

    @JsonProperty("team_name")
    @Column(name = "team_name", nullable = false)
    private String teamName;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamStandings that = (TeamStandings) o;
        return Objects.equals(getSessionKey(), that.getSessionKey())
                && Objects.equals(teamName, that.teamName);
    }

    public int hashCode() {
        return Objects.hash(getSessionKey(), teamName);
    }

}
