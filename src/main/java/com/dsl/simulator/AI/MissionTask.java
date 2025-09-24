package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionTask {
    private String taskId;
    private String taskType;
    private String assignedSatellite;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double successProbability;
    private double powerRequired;
    private double fuelRequired;
    private String priority;
}
