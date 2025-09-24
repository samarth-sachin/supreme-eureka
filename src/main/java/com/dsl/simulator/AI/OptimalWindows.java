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
public class OptimalWindows {
    private String satelliteId;
    private List<TimeWindow> optimalWindows;
    private double averageEfficiency;
    private double predictionAccuracy;
}
