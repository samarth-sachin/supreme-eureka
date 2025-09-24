package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollisionRiskForecast {
    private String satelliteId;
    private int forecastDays;
    private List<DailyRiskAssessment> dailyRisks;
    private String overallRiskLevel;
    private double aiConfidence;
}
