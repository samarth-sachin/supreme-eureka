package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRiskAssessment {
    private int day;
    private double riskProbability;
    private int threateningObjects;
    private String riskLevel;
}
