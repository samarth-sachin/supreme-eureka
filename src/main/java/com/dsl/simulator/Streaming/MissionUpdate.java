package com.dsl.simulator.Streaming;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionUpdate {

    private String missionId;
    private String missionName;
    private MissionStatus status;
    private String details;
    private double completionPercentage;
    private Instant timestamp;
    private Instant startTime;
    private Instant estimatedEndTime;
    private List<String> involvedSatellites;
    private Map<String, Object> metrics;
    private String currentPhase;
    private String nextPhase;

    public enum MissionStatus {
        PLANNING,           // Mission is being planned
        SCHEDULED,          // Mission is scheduled
        IN_PROGRESS,        // Mission is currently executing
        PAUSED,            // Mission is temporarily paused
        COMPLETED,         // Mission completed successfully
        FAILED,            // Mission failed
        CANCELLED,         // Mission was cancelled
        POSTPONED,         // Mission postponed to later date
        UNDER_REVIEW       // Mission under post-execution review
    }

    public boolean isActive() {
        return status == MissionStatus.IN_PROGRESS ||
                status == MissionStatus.SCHEDULED ||
                status == MissionStatus.PAUSED;
    }

    public boolean isCompleted() {
        return status == MissionStatus.COMPLETED ||
                status == MissionStatus.FAILED ||
                status == MissionStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return String.format("Mission[%s: %s - %.1f%%]",
                missionName, status, completionPercentage);
    }
}
