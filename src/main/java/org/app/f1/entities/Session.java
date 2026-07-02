package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "race_session",
        uniqueConstraints = @UniqueConstraint(columnNames = "session_key"),
        indexes = @Index(name = "idx_session_meeting", columnList = "meeting_id")
)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false)
    private int sessionKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "date_start", nullable = false)
    private Instant dateStart;

    @Column(name = "date_end", nullable = false)
    private Instant dateEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Column(nullable = false)
    private String sessionName;

    @ManyToMany
    @JoinTable(
            name = "session_drivers",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "driver_id")
    )
    private Set<Driver> drivers = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionKey == session.sessionKey;
    }

    @Override
    public int hashCode() { return Objects.hash(sessionKey); }
}
