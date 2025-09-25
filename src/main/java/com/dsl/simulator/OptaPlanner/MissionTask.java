package com.dsl.simulator.OptaPlanner;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PlanningEntity
public class MissionTask {

    @PlanningId  // ← THIS WAS MISSING!
    private String taskId;

    private String taskType;
    private int durationMinutes;
    private int requiredPower;
    private int requiredFuel;

    @PlanningVariable(valueRangeProviderRefs = "satelliteRange")
    private SatelliteResource assignedSatellite;

    @PlanningVariable(valueRangeProviderRefs = "timeSlotRange")
    private TimeSlot assignedTimeSlot;

    @Override
    public String toString() {
        return String.format("Task[%s: %s @ %s via %s]",
                taskId, taskType,
                assignedTimeSlot != null ? assignedTimeSlot.toString() : "UNASSIGNED",
                assignedSatellite != null ? assignedSatellite.getName() : "UNASSIGNED");
    }
}
