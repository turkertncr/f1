package org.app.f1.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.jdbc.Expectation;

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

    @ElementCollection
    @CollectionTable(name = "sector_segments", joinColumns = @JoinColumn(name = "sector_id"))
    @Column(name = "segment_time")
    @OrderColumn(name = "segment_order")
    private List<Integer> segments = new ArrayList<>();
}
