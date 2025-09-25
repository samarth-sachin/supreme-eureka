package com.dsl.simulator.OptaPlanner;

import lombok.*;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "satelliteId")
public class SatelliteResource {
    @PlanningId
    private String satelliteId;
    private String name;
    private int maxPower;
    private int maxFuel;
    private boolean operational;

    @Override
    public String toString() {
        return name;
    }
}
