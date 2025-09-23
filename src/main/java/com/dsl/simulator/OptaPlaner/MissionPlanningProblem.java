package com.dsl.simulator.OptaPlaner;

import com.dsl.simulator.Product.Satellite;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@PlanningSolution
public class MissionPlanningProblem {

    @ValueRangeProvider(id = "timeSlotRange")
    private List<TimeSlot> timeSlots;

    @ValueRangeProvider(id = "satelliteRange")
    private List<Satellite> satellites;

    @PlanningEntityCollectionProperty
    private List<MissionTask> missionTasks;

    @PlanningScore
    private HardSoftScore score;

    // Constructor with all fields
    public MissionPlanningProblem(List<TimeSlot> timeSlots,
                                  List<Satellite> satellites,
                                  List<MissionTask> missionTasks) {
        this.timeSlots = timeSlots;
        this.satellites = satellites;
        this.missionTasks = missionTasks;
    }
}
