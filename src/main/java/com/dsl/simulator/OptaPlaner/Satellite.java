package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Satellite {

    private String id;
    private String name;
    private SatelliteType type;
    private double maxPowerCapacity; // Watts
    private double currentPowerUsage; // Watts
    private double fuelCapacity; // kg
    private double currentFuelLevel; // kg
    private SatelliteStatus status;
    private boolean isOperational;

    // Capabilities
    private boolean hasCamera;
    private boolean hasRadar;
    private boolean hasSpectrometer;
    private boolean canManeuver;

    // Current orbital parameters
    private double altitudeKm;
    private double inclinationDeg;
    private double longitudeAscendingNodeDeg;

    public double getAvailablePower() {
        return maxPowerCapacity - currentPowerUsage;
    }

    public double getAvailableFuel() {
        return currentFuelLevel;
    }

    public boolean canExecuteTask(MissionTask task) {
        return isOperational &&
                getAvailablePower() >= task.getPowerRequired() &&
                getAvailableFuel() >= task.getFuelRequired() &&
                hasRequiredCapability(task.getTaskType());
    }

    private boolean hasRequiredCapability(String taskType) {
        switch (taskType.toUpperCase()) {
            case "OBSERVATION": return hasCamera || hasRadar;
            case "SPECTRAL_ANALYSIS": return hasSpectrometer;
            case "COMMUNICATION": return true; // All satellites can communicate
            case "MANEUVER": return canManeuver;
            default: return false;
        }
    }

    public enum SatelliteType {
        EARTH_OBSERVATION,
        COMMUNICATION,
        NAVIGATION,
        SCIENTIFIC,
        MILITARY,
        COMMERCIAL
    }

    public enum SatelliteStatus {
        ACTIVE,
        STANDBY,
        MAINTENANCE,
        EMERGENCY,
        DECOMMISSIONED
    }
}
