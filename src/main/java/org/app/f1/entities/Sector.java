package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = {"lap_id", "sector"})
)
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lap_id",  nullable = false)
    private Lap lap;

    private int sector;

    private Double time;

    @Column(name = "segments")
    private List<Integer> segments;
}
