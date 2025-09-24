package com.dsl.simulator.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrbitalDecayPrediction {
    private String satelliteId;
    private double currentAltitude;
    private double decayRate; // km/month
    private int daysUntilReboost;
    private double fuelRequiredForReboost;
    private double predictionAccuracy;
}
