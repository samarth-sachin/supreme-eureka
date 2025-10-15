package com.dsl.simulator.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "satellites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Satellite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "satellite_id", nullable = false, unique = true)
    private String satelliteId;

    @Column(nullable = false)
    private String name;

    @Column(name = "norad_id", nullable = false)
    private Integer noradId;

    @Column(name = "tle_line1", columnDefinition = "TEXT")
    private String tleLine1;

    @Column(name = "tle_line2", columnDefinition = "TEXT")
    private String tleLine2;

    private Double latitude;
    private Double longitude;
    private Double altitude;

    @Column(length = 50)
    private String status = "OPERATIONAL";

    @Column(name = "deployed_at", updatable = false)
    private LocalDateTime deployedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        deployedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
