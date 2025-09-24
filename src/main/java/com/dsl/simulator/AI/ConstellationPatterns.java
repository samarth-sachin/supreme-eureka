package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstellationPatterns {
    private int satelliteCount;
    private Map<String, Double> overallTrends;
    private List<String> constellationInsights;
    private double coordinationEfficiency;
    private List<String> optimalFormations;
}
