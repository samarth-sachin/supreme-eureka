package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollisionAvoidanceResult {

    private boolean success;
    private String message;
    private List<AvoidanceManeuver> maneuvers;
    private double totalFuelCost;
    private double overallSuccessProbability;
    private long computationTimeMs;

    public static CollisionAvoidanceResult noActionRequired() {
        return CollisionAvoidanceResult.builder()
                .success(true)
                .message("No collision avoidance required")
                .totalFuelCost(0.0)
                .overallSuccessProbability(1.0)
                .build();
    }
}
