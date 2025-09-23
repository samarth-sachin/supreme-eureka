package com.dsl.simulator.OptaPlaner;

import com.dsl.simulator.Product.Satellite;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@PlanningEntity
public class MissionTask {

    private Long id;
    private String taskType; // "OBSERVATION", "COMMUNICATION", "MANEUVER"
    private String targetLocation;
    private int durationMinutes;
    private int priority; // 1-10, 10 being highest
    private double powerRequired; // Watts
    private double fuelRequired; // kg

    @PlanningVariable(valueRangeProviderRefs = "satelliteRange")
    private Satellite assignedSatellite;

    @PlanningVariable(valueRangeProviderRefs = "timeSlotRange")
    private TimeSlot assignedTimeSlot;

    public MissionTask(Long id, String taskType, String targetLocation,
                       int durationMinutes, int priority,
                       double powerRequired, double fuelRequired) {
        this.id = id;
        this.taskType = taskType;
        this.targetLocation = targetLocation;
        this.durationMinutes = durationMinutes;
        this.priority = priority;
        this.powerRequired = powerRequired;
        this.fuelRequired = fuelRequired;
    }
}
