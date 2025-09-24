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
public class SatelliteCommand {

    private String commandId;
    private String satelliteId;
    private CommandType commandType;
    private String command;
    private Map<String, Object> parameters;
    private Instant timestamp;
    private int priority; // 1-10, 10 being highest
    private long timeoutMs;
    private String sourceSystem;

    public enum CommandType {
        ATTITUDE_CONTROL,
        ORBIT_MANEUVER,
        SYSTEM_CONFIG,
        PAYLOAD_OPERATION,
        COMMUNICATION,
        POWER_MANAGEMENT,
        EMERGENCY,
        DIAGNOSTIC
    }

    @Override
    public String toString() {
        return String.format("SatCommand[%s->%s: %s]",
                sourceSystem, satelliteId, commandType);
    }
}
