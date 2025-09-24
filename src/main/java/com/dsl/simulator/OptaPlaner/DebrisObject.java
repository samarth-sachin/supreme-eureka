package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebrisObject {

    private String id;
    private String name;
    private DebrisType type;
    private double closestApproachDistance; // km
    private long timeToClosestApproach; // seconds from now
    private double collisionProbability;
    private double mass; // kg (if known)
    private double crossSectionalArea; // m²

    public boolean isHighRisk(double thresholdKm) {
        return closestApproachDistance < thresholdKm && collisionProbability > 1e-5;
    }

    public enum DebrisType {
        SATELLITE_FRAGMENT,
        ROCKET_BODY,
        MISSION_DEBRIS,
        OPERATIONAL_SATELLITE,
        UNKNOWN_OBJECT
    }

    @Override
    public String toString() {
        return String.format("Debris[%s: %.2f km in %d sec, P=%.2e]",
                name, closestApproachDistance, timeToClosestApproach, collisionProbability);
    }
}
