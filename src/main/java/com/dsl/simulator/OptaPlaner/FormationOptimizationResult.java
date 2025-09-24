package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationOptimizationResult {

    private boolean success;
    private String formationType;
    private String message;
    private Map<String, FormationPosition> positions;
    private double totalFuelCost;
    private double stabilityScore;
    private double maintenanceComplexity;

    public static FormationOptimizationResult failed(String message) {
        return FormationOptimizationResult.builder()
                .success(false)
                .message(message)
                .stabilityScore(0.0)
                .build();
    }
}
