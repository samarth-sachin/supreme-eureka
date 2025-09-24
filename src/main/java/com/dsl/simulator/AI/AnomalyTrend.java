package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyTrend {
    private String satelliteId;
    private int analysisPeriodDays;
    private int totalAnomalies;
    private Map<String, Integer> anomaliesByType;
    private String trendDirection; // INCREASING, DECREASING, STABLE
    private double averageAnomaliesPerDay;
    private String mostCommonAnomaly;
}
