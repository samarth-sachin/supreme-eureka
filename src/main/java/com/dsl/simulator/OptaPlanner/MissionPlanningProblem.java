package com.dsl.simulator.OptaPlanner;

import java.util.List;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PlanningSolution
public class MissionPlanningProblem {

    @PlanningEntityCollectionProperty
    private List<MissionTask> taskList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "satelliteRange")
    private List<SatelliteResource> satelliteList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeSlotRange")
    private List<TimeSlot> timeSlotList;

    @PlanningScore
    private HardSoftScore score;
}
