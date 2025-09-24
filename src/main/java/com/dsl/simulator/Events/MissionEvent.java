package com.dsl.simulator.Events;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionEvent {

    private String missionId;
    private MissionStatus status;
    private String details;
    private double completionPercentage;
    private Instant timestamp;

    public enum MissionStatus {
        PLANNING,
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
        POSTPONED
    }
}
