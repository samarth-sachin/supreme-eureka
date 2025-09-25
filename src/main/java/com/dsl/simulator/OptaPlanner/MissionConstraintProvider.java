package com.dsl.simulator.OptaPlanner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;

public class MissionConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                satelliteDoubleBooking(constraintFactory),
                powerOveruse(constraintFactory),
                fuelOveruse(constraintFactory),
                maximizeTaskAssignment(constraintFactory)
        };
    }

    // HARD constraint: Satellite cannot be double-booked at the same time
    private Constraint satelliteDoubleBooking(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(MissionTask.class)
                .join(MissionTask.class)
                .filter((task1, task2) ->
                        !task1.getTaskId().equals(task2.getTaskId()) && // Different tasks
                                task1.getAssignedSatellite() != null &&
                                task2.getAssignedSatellite() != null &&
                                task1.getAssignedSatellite().getSatelliteId().equals(task2.getAssignedSatellite().getSatelliteId()) &&
                                task1.getAssignedTimeSlot() != null &&
                                task2.getAssignedTimeSlot() != null &&
                                task1.getAssignedTimeSlot().getTimeSlotId().equals(task2.getAssignedTimeSlot().getTimeSlotId()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Double-booking satellite");
    }

    // HARD constraint: Power limit per satellite per time slot
    private Constraint powerOveruse(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(MissionTask.class)
                .filter(task -> task.getAssignedSatellite() != null && task.getAssignedTimeSlot() != null)
                .groupBy(MissionTask::getAssignedSatellite,
                        MissionTask::getAssignedTimeSlot,
                        ConstraintCollectors.sum(MissionTask::getRequiredPower))
                .filter((satellite, timeSlot, totalPower) -> totalPower > satellite.getMaxPower())
                .penalize(HardSoftScore.ONE_HARD,
                        (satellite, timeSlot, totalPower) -> totalPower - satellite.getMaxPower())
                .asConstraint("Power over limit");
    }

    // HARD constraint: Fuel limit per satellite per time slot
    private Constraint fuelOveruse(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(MissionTask.class)
                .filter(task -> task.getAssignedSatellite() != null && task.getAssignedTimeSlot() != null)
                .groupBy(MissionTask::getAssignedSatellite,
                        MissionTask::getAssignedTimeSlot,
                        ConstraintCollectors.sum(MissionTask::getRequiredFuel))
                .filter((satellite, timeSlot, totalFuel) -> totalFuel > satellite.getMaxFuel())
                .penalize(HardSoftScore.ONE_HARD,
                        (satellite, timeSlot, totalFuel) -> totalFuel - satellite.getMaxFuel())
                .asConstraint("Fuel over limit");
    }

    // SOFT constraint: Maximize assigned tasks
    private Constraint maximizeTaskAssignment(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(MissionTask.class)
                .filter(task -> task.getAssignedSatellite() != null)
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Maximize assigned tasks");
    }
}
