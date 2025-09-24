package com.dsl.simulator.Events;

import lombok.Data;
import lombok.Builder;
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
    private Instant timestamp;
    private Map<String, Object> metadata;
    private boolean acknowledged;
    private String acknowledgedBy;
    private Instant acknowledgedAt;

    public enum AlertLevel {
        INFO,
        WARNING,
        ERROR,
        CRITICAL,
        EMERGENCY
    }
}
