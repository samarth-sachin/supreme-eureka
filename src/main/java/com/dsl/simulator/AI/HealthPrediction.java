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
public class HealthPrediction {
    private String satelliteId;
    private double overallHealth; // 0.0 to 1.0
    private Map<String, Double> systemHealth; // System name -> health score
    private List<PredictedFailure> predictedFailures;
    private double predictionConfidence;
    private long analysisTimestamp;
}
