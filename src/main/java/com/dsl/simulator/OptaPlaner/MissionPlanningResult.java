package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionPlanningResult {

    private boolean success;
    private String score;
    private String message;
    private List<MissionTask> optimizedTasks;
    private double totalPowerUsage;
    private double totalFuelUsage;
    private double completionRate;
    private long solutionTimeMs;

    public static MissionPlanningResult failed(String errorMessage) {
        return MissionPlanningResult.builder()
                .success(false)
                .message(errorMessage)
                .completionRate(0.0)
                .build();
    }

    public static MissionPlanningResult success(String score, List<MissionTask> tasks) {
        return MissionPlanningResult.builder()
                .success(true)
                .score(score)
                .optimizedTasks(tasks)
                .message("Optimization completed successfully")
                .build();
    }
}
