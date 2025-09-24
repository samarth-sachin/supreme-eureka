package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictedFailure {
    private String component;
    private int daysUntilFailure;
    private double confidence;
    private String failureType;
    private String impact;
}
