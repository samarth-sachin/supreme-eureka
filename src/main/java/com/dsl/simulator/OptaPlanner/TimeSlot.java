package com.dsl.simulator.OptaPlanner;

import org.optaplanner.core.api.domain.lookup.PlanningId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"startHour", "endHour"})
public class TimeSlot {

    private int startHour;
    private int endHour;

    @PlanningId  // ← ADD THIS FOR UNIQUE IDENTIFICATION
    public String getTimeSlotId() {
        return String.format("%02d-%02d", startHour, endHour);
    }

    @Override
    public String toString() {
        return String.format("%02d:00-%02d:00", startHour, endHour);
    }
}
