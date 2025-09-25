package com.dsl.simulator.OptaPlanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class OptaPlannerMissionService {

    private final AtomicLong problemIdCounter = new AtomicLong(0);

    public MissionPlanningProblem solveMissionPlan(List<MissionTask> tasks,
                                                   List<SatelliteResource> satellites,
                                                   List<TimeSlot> slots) {

        Long problemId = problemIdCounter.incrementAndGet();
        log.info("🚀 Starting OptaPlanner solver #{} with {} tasks, {} satellites, {} time slots",
                problemId, tasks.size(), satellites.size(), slots.size());

        try {
            // Create simple solver configuration
            SolverConfig solverConfig = new SolverConfig()
                    .withSolutionClass(MissionPlanningProblem.class)
                    .withEntityClasses(MissionTask.class)
                    .withConstraintProviderClass(MissionConstraintProvider.class)
                    .withTerminationSpentLimit(java.time.Duration.ofSeconds(3));

            SolverFactory<MissionPlanningProblem> solverFactory = SolverFactory.create(solverConfig);
            Solver<MissionPlanningProblem> solver = solverFactory.buildSolver();

            MissionPlanningProblem planningProblem = MissionPlanningProblem.builder()
                    .taskList(tasks)
                    .satelliteList(satellites)
                    .timeSlotList(slots)
                    .build();

            // Solve synchronously
            MissionPlanningProblem solution = solver.solve(planningProblem);

            log.info("✅ OptaPlanner solver #{} completed with score: {}", problemId, solution.getScore());
            return solution;

        } catch (Exception e) {
            log.error("❌ OptaPlanner solver #{} failed: {}", problemId, e.getMessage());

            // Return unsolved problem as fallback
            return MissionPlanningProblem.builder()
                    .taskList(tasks)
                    .satelliteList(satellites)
                    .timeSlotList(slots)
                    .build();
        }
    }

    public String formatSolutionResults(MissionPlanningProblem solution) {
        StringBuilder result = new StringBuilder();
        result.append("🎯 === REAL OPTAPLANNER RESULTS ===\n");
        result.append("Final Score: ").append(solution.getScore() != null ? solution.getScore() : "Not scored").append("\n");

        int assignedTasks = (int) solution.getTaskList().stream()
                .mapToInt(task -> task.getAssignedSatellite() != null ? 1 : 0)
                .sum();

        result.append("Tasks Assigned: ").append(assignedTasks)
                .append("/").append(solution.getTaskList().size()).append("\n\n");

        result.append("📋 TASK ASSIGNMENTS:\n");
        for (MissionTask task : solution.getTaskList()) {
            if (task.getAssignedSatellite() != null && task.getAssignedTimeSlot() != null) {
                result.append("   ✅ ").append(task.toString()).append("\n");
                result.append("      Power: ").append(task.getRequiredPower()).append("W, ");
                result.append("Fuel: ").append(task.getRequiredFuel()).append("kg\n");
            } else {
                result.append("   ❌ ").append(task.getTaskId()).append(": UNASSIGNED\n");
            }
        }

        result.append("\n🎯 OPTIMIZATION STATUS: ");
        if (solution.getScore() != null) {
            result.append(solution.getScore().isFeasible() ? "FEASIBLE" : "INFEASIBLE");
        } else {
            result.append("PROCESSING");
        }
        result.append("\n================================");

        return result.toString();
    }
}
