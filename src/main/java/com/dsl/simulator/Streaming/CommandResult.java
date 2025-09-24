package com.dsl.simulator.Streaming;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResult {

    private String commandId;
    private String satelliteId;
    private CommandStatus status;
    private String result;
    private String errorMessage;
    private long executionTime; // milliseconds
    private Instant startTime;
    private Instant endTime;
    private double successProbability;

    public enum CommandStatus {
        RECEIVED,       // Command received but not started
        QUEUED,         // Command queued for execution
        EXECUTING,      // Command currently executing
        COMPLETED,      // Command completed successfully
        FAILED,         // Command failed to execute
        TIMEOUT,        // Command timed out
        CANCELLED,      // Command was cancelled
        PARTIAL_SUCCESS // Command partially succeeded
    }

    public boolean isSuccessful() {
        return status == CommandStatus.COMPLETED ||
                status == CommandStatus.PARTIAL_SUCCESS;
    }

    public boolean isFinalState() {
        return status == CommandStatus.COMPLETED ||
                status == CommandStatus.FAILED ||
                status == CommandStatus.TIMEOUT ||
                status == CommandStatus.CANCELLED ||
                status == CommandStatus.PARTIAL_SUCCESS;
    }

    @Override
    public String toString() {
        return String.format("CommandResult[%s: %s in %dms]",
                commandId, status, executionTime);
    }
}
