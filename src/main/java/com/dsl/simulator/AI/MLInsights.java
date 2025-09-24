package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLInsights {
    private double modelAccuracy;
    private double overallConfidence;
    private List<String> keyInsights;
    private Map<String, Double> featureImportance;
    private String modelVersion;
    private long trainingDataPoints;
    private Instant lastModelUpdate;
}
