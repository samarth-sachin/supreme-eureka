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
public class CommandEvent {

    private String satelliteId;
    private String commandId;
    private CommandStatus status;
    private String result;
    private Instant timestamp;
    private long executionTimeMs;

    public enum CommandStatus {
        RECEIVED,
        EXECUTING,
        COMPLETED,
        FAILED,
        TIMEOUT
    }
}
