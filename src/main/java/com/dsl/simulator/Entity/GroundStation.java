package com.dsl.simulator.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ground_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroundStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false, unique = true)
    private String stationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double elevation = 0.0;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(name = "deployed_at", updatable = false)
    private LocalDateTime deployedAt;

    @PrePersist
    protected void onCreate() {
        deployedAt = LocalDateTime.now();
    }
}
