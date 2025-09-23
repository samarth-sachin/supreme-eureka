package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public boolean overlaps(TimeSlot other) {
        return !this.endTime.isBefore(other.startTime) &&
                !other.endTime.isBefore(this.startTime);
    }

    @Override
    public String toString() {
        return String.format("TimeSlot[%s - %s]", startTime, endTime);
    }
}
