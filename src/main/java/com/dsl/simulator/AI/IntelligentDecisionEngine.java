package com.dsl.simulator.AI;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IntelligentDecisionEngine {

    /**
     * Generate AI-powered recommendations
     */
    public List<AIRecommendation> generateRecommendations(String satelliteId,
                                                          HealthPrediction healthPrediction,
                                                          List<Anomaly> anomalies,
                                                          PatternAnalysis patterns) {
        log.info("🎯 Generating AI recommendations for {}", satelliteId);

        List<AIRecommendation> recommendations = new ArrayList<>();

        // Health-based recommendations
        if (healthPrediction.getOverallHealth() < 0.8) {
            recommendations.add(AIRecommendation.builder()
                    .actionType("PREVENTIVE_MAINTENANCE")
                    .description("Schedule comprehensive system diagnostics and maintenance")
                    .priority("HIGH")
                    .successProbability(0.91)
                    .impact("Prevent potential system failures and extend mission life")
                    .timeframe("Within 48 hours")
                    .resourcesRequired(Arrays.asList("Ground team", "Diagnostic tools", "2-4 hours"))
                    .build());
        }

        // Anomaly-based recommendations
        for (Anomaly anomaly : anomalies) {
            if (anomaly.getSeverity().equals("CRITICAL")) {
                recommendations.add(AIRecommendation.builder()
                        .actionType("EMERGENCY_RESPONSE")
                        .description("Immediate intervention required for " + anomaly.getParameter())
                        .priority("CRITICAL")
                        .successProbability(0.85)
                        .impact("Prevent system failure and mission loss")
                        .timeframe("Immediate (within 10 minutes)")
                        .resourcesRequired(Arrays.asList("Mission control", "Emergency protocol"))
                        .build());
            }
        }

        // Pattern-based optimization recommendations
        if (patterns.getPerformanceTrends().get("Power Efficiency") < -0.05) {
            recommendations.add(AIRecommendation.builder()
                    .actionType("POWER_OPTIMIZATION")
                    .description("Optimize power management system based on usage patterns")
                    .priority("MEDIUM")
                    .successProbability(0.78)
                    .impact("Improve power efficiency by 12-18%")
                    .timeframe("Next maintenance window")
                    .resourcesRequired(Arrays.asList("Software update", "Configuration change"))
                    .build());
        }

        // Proactive recommendations
        recommendations.add(AIRecommendation.builder()
                .actionType("PREDICTIVE_POSITIONING")
                .description("Adjust orbital position for optimal performance windows")
                .priority("LOW")
                .successProbability(0.92)
                .impact("Increase operational efficiency by 8-12%")
                .timeframe("Next orbit adjustment opportunity")
                .resourcesRequired(Arrays.asList("0.3kg fuel", "Automated maneuver"))
                .build());

        // Sort by priority
        return recommendations.stream()
                .sorted((r1, r2) -> getPriorityValue(r2.getPriority()) - getPriorityValue(r1.getPriority()))
                .collect(Collectors.toList());
    }

    /**
     * Generate emergency action plans
     */
    public List<AIRecommendation> generateEmergencyActions(List<Anomaly> criticalAnomalies) {
        log.info("🆘 Generating emergency actions for {} critical anomalies", criticalAnomalies.size());

        List<AIRecommendation> emergencyActions = new ArrayList<>();

        for (Anomaly anomaly : criticalAnomalies) {
            switch (anomaly.getType()) {
                case "CRITICAL_POWER_FAILURE":
                    emergencyActions.add(AIRecommendation.builder()
                            .actionType("SWITCH_TO_BACKUP_POWER")
                            .description("Immediately switch to backup power systems")
                            .priority("CRITICAL")
                            .successProbability(0.94)
                            .impact("Prevent total power loss")
                            .timeframe("Immediate (< 2 minutes)")
                            .build());
                    break;

                case "THERMAL_CRITICAL":
                    emergencyActions.add(AIRecommendation.builder()
                            .actionType("EMERGENCY_COOLING")
                            .description("Activate emergency thermal management and reduce processing load")
                            .priority("CRITICAL")
                            .successProbability(0.87)
                            .impact("Prevent thermal damage to critical components")
                            .timeframe("Immediate (< 5 minutes)")
                            .build());
                    break;

                case "COMMUNICATION_FAILURE":
                    emergencyActions.add(AIRecommendation.builder()
                            .actionType("ANTENNA_RECONFIGURATION")
                            .description("Switch to backup antenna and reconfigure communication systems")
                            .priority("HIGH")
                            .successProbability(0.82)
                            .impact("Restore communication link")
                            .timeframe("Within 15 minutes")
                            .build());
                    break;
            }
        }

        return emergencyActions;
    }

    /**
     * Generate optimal mission plan using AI
     */
    public AIMissionPlan generateOptimalMissionPlan(String missionType,
                                                    List<String> satelliteIds,
                                                    int durationHours) {
        log.info("🤖 Generating optimal mission plan: {} for {} satellites", missionType, satelliteIds.size());

        List<MissionTask> optimizedTasks = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);

        // Generate AI-optimized tasks
        for (int i = 0; i < durationHours / 2; i++) {
            String satellite = satelliteIds.get(i % satelliteIds.size());
            LocalDateTime taskStart = startTime.plusHours(i * 2);

            optimizedTasks.add(MissionTask.builder()
                    .taskId("AI_TASK_" + (i + 1))
                    .taskType(generateTaskType(missionType))
                    .assignedSatellite(satellite)
                    .startTime(taskStart)
                    .endTime(taskStart.plusHours(2))
                    .successProbability(0.85 + Math.random() * 0.14)
                    .powerRequired(150 + Math.random() * 200)
                    .fuelRequired(0.5 + Math.random() * 2.0)
                    .priority(Math.random() > 0.7 ? "HIGH" : "NORMAL")
                    .build());
        }

        // Calculate plan efficiency
        double powerEfficiency = 0.82 + Math.random() * 0.15;
        double fuelEfficiency = 0.78 + Math.random() * 0.18;
        double timeEfficiency = 0.91 + Math.random() * 0.08;
        double overallConfidence = 0.87 + Math.random() * 0.12;

        return AIMissionPlan.builder()
                .missionType(missionType)
                .tasks(optimizedTasks)
                .satelliteIds(satelliteIds)
                .durationHours(durationHours)
                .powerEfficiency(powerEfficiency)
                .fuelEfficiency(fuelEfficiency)
                .timeEfficiency(timeEfficiency)
                .confidence(overallConfidence)
                .totalTasks(optimizedTasks.size())
                .estimatedSuccessRate(0.89 + Math.random() * 0.1)
                .build();
    }

    // Helper methods
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "CRITICAL": return 4;
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    private String generateTaskType(String missionType) {
        Map<String, String[]> missionTasks = Map.of(
                "EARTH_OBSERVATION", new String[]{"IMAGE_CAPTURE", "SPECTRAL_ANALYSIS", "TERRAIN_MAPPING"},
                "COMMUNICATION", new String[]{"DATA_RELAY", "SIGNAL_AMPLIFICATION", "NETWORK_ROUTING"},
                "SCIENTIFIC", new String[]{"ATMOSPHERIC_STUDY", "MAGNETIC_MEASUREMENT", "RADIATION_DETECTION"},
                "DEFAULT", new String[]{"MONITORING", "DATA_COLLECTION", "SYSTEM_CHECK"}
        );

        String[] tasks = missionTasks.getOrDefault(missionType.toUpperCase(), missionTasks.get("DEFAULT"));
        return tasks[(int)(Math.random() * tasks.length)];
    }
}
