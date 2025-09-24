package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindow {
    private String startTime;
    private String endTime;
    private String activity;
    private double efficiency;
    private String reason;
}
