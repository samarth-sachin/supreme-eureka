package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendation {
    private String actionType;
    private String description;
    private String priority;
    private double successProbability;
    private String impact;
    private String timeframe;
    private List<String> resourcesRequired;
}
