package com.dsl.simulator.Events;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEvent {

    private String satelliteId;
    private Instant timestamp;
    private SatelliteTelemetry telemetry;
    private String eventType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SatelliteTelemetry {
        private double batteryLevel; // percentage
        private double powerGeneration; // watts
        private double fuelLevel; // kg
        private double temperature; // celsius
        private Position position;
        private Velocity velocity;
        private AttitudeData attitude;
        private boolean[] systemStatus; //各サブシステムの状態

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Position {
            private double x; // km
            private double y; // km
            private double z; // km
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Velocity {
            private double vx; // km/s
            private double vy; // km/s
            private double vz; // km/s
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AttitudeData {
            private double roll; // degrees
            private double pitch; // degrees
            private double yaw; // degrees
        }
    }
}
