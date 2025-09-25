package com.dsl.simulator.Service;

import com.dsl.simulator.OptaPlanner.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConstellationOptimizer {

    private final OptaPlannerMissionService optaPlannerService;

    /**
     * REAL OptaPlanner-based formation optimization (replaces fake version)
     */
    public String optimizeFormation(List<String> satelliteIds, String formationType, double separation) {
        log.info("🎯 REAL OptaPlanner Formation Optimization: {} satellites in {} formation",
                satelliteIds.size(), formationType);

        try {
            // Create satellites
            List<SatelliteResource> satellites = new ArrayList<>();
            for (int i = 0; i < satelliteIds.size(); i++) {
                satellites.add(SatelliteResource.builder()
                        .satelliteId(satelliteIds.get(i))
                        .name(satelliteIds.get(i))
                        .maxPower(1000 + i * 100) // Different capacities
                        .maxFuel(500 + i * 50)
                        .operational(true)
                        .build());
            }

            // Create formation tasks
            List<MissionTask> tasks = createFormationTasks(formationType, satelliteIds.size());

            // Create time slots (24 hours)
            List<TimeSlot> timeSlots = new ArrayList<>();
            for (int hour = 0; hour < 4; hour++) { // ← ONLY 4 HOURS!
                timeSlots.add(TimeSlot.builder()
                        .startHour(hour)
                        .endHour(hour + 1)
                        .build());
            }

            // Solve with real OptaPlanner
            MissionPlanningProblem solution = optaPlannerService.solveMissionPlan(tasks, satellites, timeSlots);

            return formatFormationResults(solution, formationType, separation);

        } catch (Exception e) {
            log.error("❌ OptaPlanner formation optimization failed: {}", e.getMessage());
            return "❌ Formation optimization failed: " + e.getMessage();
        }
    }

    /**
     * REAL OptaPlanner-based mission planning (replaces fake version)
     */
    public String optimizeMissionPlan(int durationHours) {
        log.info("📋 REAL OptaPlanner Mission Planning for {} hours", durationHours);

        try {
            // Create default satellites
            List<SatelliteResource> satellites = List.of(
                    SatelliteResource.builder()
                            .satelliteId("SAT_Alpha")
                            .name("Alpha Satellite")
                            .maxPower(1200)
                            .maxFuel(600)
                            .operational(true)
                            .build(),
                    SatelliteResource.builder()
                            .satelliteId("SAT_Beta")
                            .name("Beta Satellite")
                            .maxPower(1000)
                            .maxFuel(500)
                            .operational(true)
                            .build()
            );

            // Create mission tasks
            List<MissionTask> tasks = createMissionTasks(durationHours * 2); // 2 tasks per hour

            // Create time slots
            List<TimeSlot> timeSlots = new ArrayList<>();
            for (int hour = 0; hour < durationHours; hour++) {
                timeSlots.add(TimeSlot.builder()
                        .startHour(hour)
                        .endHour(hour + 1)
                        .build());
            }

            // Solve with real OptaPlanner
            MissionPlanningProblem solution = optaPlannerService.solveMissionPlan(tasks, satellites, timeSlots);

            return formatMissionPlanResults(solution, durationHours);

        } catch (Exception e) {
            log.error("❌ OptaPlanner mission planning failed: {}", e.getMessage());
            return "❌ Mission planning failed: " + e.getMessage();
        }
    }

    // Helper methods for creating tasks
    private List<MissionTask> createFormationTasks(String formationType, int satelliteCount) {
        List<MissionTask> tasks = new ArrayList<>();
        String[] taskTypes = {"POSITION_MAINTAIN", "FORMATION_ADJUST"};

        // ONLY CREATE 1 TASK PER SATELLITE (instead of 3)
        for (int i = 0; i < satelliteCount; i++) { // ← REDUCED FROM satelliteCount * 3
            tasks.add(MissionTask.builder()
                    .taskId("FORM_TASK_" + (i + 1))
                    .taskType(taskTypes[i % taskTypes.length])
                    .durationMinutes(60)
                    .requiredPower(150 + (i * 25))
                    .requiredFuel(10 + (i * 2))
                    .build());
        }

        return tasks;
    }


    private List<MissionTask> createMissionTasks(int taskCount) {
        List<MissionTask> tasks = new ArrayList<>();
        String[] taskTypes = {"EARTH_OBSERVATION", "DATA_RELAY", "SYSTEM_CHECK", "COMMUNICATION"};

        for (int i = 0; i < taskCount; i++) {
            tasks.add(MissionTask.builder()
                    .taskId("MISSION_TASK_" + (i + 1))
                    .taskType(taskTypes[i % taskTypes.length])
                    .durationMinutes(120)
                    .requiredPower(200 + (i * 50))
                    .requiredFuel(15 + (i * 3))
                    .build());
        }

        return tasks;
    }

    // Formatting methods
    private String formatFormationResults(MissionPlanningProblem solution, String formationType, double separation) {
        StringBuilder result = new StringBuilder();
        result.append("🛰️ === REAL OPTAPLANNER FORMATION OPTIMIZATION ===\n");
        result.append("Formation Type: ").append(formationType).append("\n");
        result.append("Separation: ").append(separation).append(" km\n");
        result.append("OptaPlanner Score: ").append(solution.getScore()).append("\n");
        result.append("Tasks Assigned: ").append(
                        solution.getTaskList().stream()
                                .mapToInt(task -> task.getAssignedSatellite() != null ? 1 : 0)
                                .sum())
                .append("/").append(solution.getTaskList().size()).append("\n\n");

        result.append("📋 FORMATION ASSIGNMENTS:\n");
        for (MissionTask task : solution.getTaskList()) {
            if (task.getAssignedSatellite() != null) {
                result.append("   ✅ ").append(task.toString()).append("\n");
                result.append("      Power: ").append(task.getRequiredPower()).append("W, ");
                result.append("Fuel: ").append(task.getRequiredFuel()).append("kg\n");
            }
        }

        result.append("🎯 OPTIMIZATION STATUS: ").append(solution.getScore().isFeasible() ? "FEASIBLE" : "INFEASIBLE");
        result.append("\n===============================================");

        return result.toString();
    }

    private String formatMissionPlanResults(MissionPlanningProblem solution, int durationHours) {
        StringBuilder result = new StringBuilder();
        result.append("📋 === REAL OPTAPLANNER MISSION PLANNING ===\n");
        result.append("Duration: ").append(durationHours).append(" hours\n");
        result.append("OptaPlanner Score: ").append(solution.getScore()).append("\n");
        result.append("Tasks Successfully Assigned: ").append(
                        solution.getTaskList().stream()
                                .mapToInt(task -> task.getAssignedSatellite() != null ? 1 : 0)
                                .sum())
                .append("/").append(solution.getTaskList().size()).append("\n\n");

        result.append("📅 OPTIMIZED SCHEDULE:\n");
        for (MissionTask task : solution.getTaskList()) {
            if (task.getAssignedSatellite() != null && task.getAssignedTimeSlot() != null) {
                result.append("   ✅ ").append(task.getTaskType())
                        .append(" @ ").append(task.getAssignedTimeSlot())
                        .append(" via ").append(task.getAssignedSatellite().getName()).append("\n");
            }
        }

        result.append("\n🎯 OPTIMIZATION STATUS: ").append(solution.getScore().isFeasible() ? "FEASIBLE" : "INFEASIBLE");
        result.append("\n==========================================");

        return result.toString();
    }
}
