package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anomaly {
    private String satelliteId;
    private String type;
    private String parameter;
    private String currentValue;
    private String expectedValue;
    private double confidence;
    private String severity;
    private String description;
    private Instant detectionTime;
    private String likelyCause;
    private String recommendation;
    private int timeToAction; // seconds
}
