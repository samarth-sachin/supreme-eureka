package com.dsl.simulator.Streaming;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteAlert {

    private String alertId;
    private String satelliteId;
    private AlertLevel alertLevel;
    private String message;
    private String description;
    private Instant timestamp;
    private Map<String, Object> metadata;
    private boolean acknowledged;
    private String acknowledgedBy;
    private Instant acknowledgedAt;
    private String category;
    private String subsystem;

    public enum AlertLevel {
        INFO,       // Informational messages
        WARNING,    // Potential issues
        ERROR,      // System errors
        CRITICAL,   // Critical system failures
        EMERGENCY   // Immediate action required
    }

    public boolean requiresImmediateAction() {
        return alertLevel == AlertLevel.CRITICAL || alertLevel == AlertLevel.EMERGENCY;
    }

    @Override
    public String toString() {
        return String.format("Alert[%s-%s: %s]",
                satelliteId, alertLevel, message);
    }
}
