package com.dsl.simulator.Streaming;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class TelemetryData {
    private String satelliteId;
    private Instant timestamp;
    private double batteryLevel; // percentage
    private double powerGeneration; // watts
    private double fuelLevel; // percentage
    private double temperature; // celsius
    private PositionData position;
    private VelocityData velocity;
    private AttitudeData attitude;
    private SystemStatus systemStatus;
    private double dataRate; // Mbps
    private double signalStrength; // dBm
}

@Data
@Builder
class PositionData {
    private double x, y, z; // km
}

@Data
@Builder
class VelocityData {
    private double vx, vy, vz; // km/s
}

@Data
@Builder
class AttitudeData {
    private double roll, pitch, yaw; // degrees
}

@Data
@Builder
class SystemStatus {
    private boolean communicationSystem;
    private boolean powerSystem;
    private boolean propulsionSystem;
    private boolean payloadSystem;
    private boolean attitudeControlSystem;
}
