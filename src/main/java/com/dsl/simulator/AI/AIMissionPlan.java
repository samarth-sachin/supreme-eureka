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
public class AIMissionPlan {
    private String missionType;
    private List<MissionTask> tasks;
    private List<String> satelliteIds;
    private int durationHours;
    private double powerEfficiency;
    private double fuelEfficiency;
    private double timeEfficiency;
    private double confidence;
    private int totalTasks;
    private double estimatedSuccessRate;
}
