package com.dsl.simulator.Streaming;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryStreamer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, Boolean> activeStreams = new ConcurrentHashMap<>();

    // Kafka Topics
    private static final String TELEMETRY_TOPIC = "satellite-telemetry";
    private static final String ALERTS_TOPIC = "satellite-alerts";
    private static final String COMMANDS_TOPIC = "satellite-commands";
    private static final String MISSION_UPDATES_TOPIC = "mission-updates";

    /**
     * Start real-time telemetry streaming for a satellite
     */
    public String startTelemetryStream(String satelliteId) {
        if (activeStreams.getOrDefault(satelliteId, false)) {
            return "⚠️ Telemetry stream already active for " + satelliteId;
        }

        activeStreams.put(satelliteId, true);
        log.info("📡 Starting telemetry stream for {}", satelliteId);

        // Stream telemetry every 5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            if (activeStreams.getOrDefault(satelliteId, false)) {
                streamTelemetryData(satelliteId);
            }
        }, 0, 5, TimeUnit.SECONDS);

        return "✅ Real-time telemetry streaming started for " + satelliteId;
    }

    /**
     * Stop telemetry streaming
     */
    public String stopTelemetryStream(String satelliteId) {
        activeStreams.put(satelliteId, false);
        log.info("🛑 Stopping telemetry stream for {}", satelliteId);
        return "✅ Telemetry streaming stopped for " + satelliteId;
    }

    /**
     * Stream live telemetry data
     */
    private void streamTelemetryData(String satelliteId) {
        try {
            TelemetryData telemetry = generateRealisticTelemetry(satelliteId);

            kafkaTemplate.send(TELEMETRY_TOPIC, satelliteId, telemetry)
                    .whenComplete((result, failure) -> {
                        if (failure != null) {
                            log.error("❌ Failed to stream telemetry for {}: {}", satelliteId, failure.getMessage());
                        } else {
                            log.debug("📊 Telemetry streamed for {}", satelliteId);
                        }
                    });

            // Check for alerts
            checkAndStreamAlerts(satelliteId, telemetry);

        } catch (Exception e) {
            log.error("❌ Error streaming telemetry for {}: {}", satelliteId, e.getMessage());
        }
    }

    /**
     * Generate realistic telemetry data
     */
    private TelemetryData generateRealisticTelemetry(String satelliteId) {
        return TelemetryData.builder()
                .satelliteId(satelliteId)
                .timestamp(Instant.now())
                .batteryLevel(85.0 + Math.random() * 10 - 5) // 80-90%
                .powerGeneration(200 + Math.random() * 100) // 200-300W
                .fuelLevel(70.0 + Math.random() * 20) // 70-90%
                .temperature(20 + Math.random() * 30 - 15) // 5-35°C
                .position(generateOrbitPosition())
                .velocity(generateOrbitVelocity())
                .attitude(generateAttitudeData())
                .systemStatus(generateSystemStatus())
                .dataRate(1000 + Math.random() * 5000) // 1-6 Mbps
                .signalStrength(-65 - Math.random() * 20) // -65 to -85 dBm
                .build();
    }

    /**
     * Check telemetry for alerts and stream them
     */
    private void checkAndStreamAlerts(String satelliteId, TelemetryData telemetry) {
        // Battery level alert
        if (telemetry.getBatteryLevel() < 20) {
            streamAlert(satelliteId, SatelliteAlert.AlertLevel.CRITICAL,
                    "Battery level critically low: " + String.format("%.1f%%", telemetry.getBatteryLevel()),
                    Map.of("batteryLevel", telemetry.getBatteryLevel()));
        }

        // Temperature alert
        if (telemetry.getTemperature() > 60 || telemetry.getTemperature() < -20) {
            streamAlert(satelliteId, SatelliteAlert.AlertLevel.WARNING,
                    "Temperature out of normal range: " + String.format("%.1f°C", telemetry.getTemperature()),
                    Map.of("temperature", telemetry.getTemperature()));
        }
    }

    /**
     * Stream alert to Kafka
     */
    public void streamAlert(String satelliteId, SatelliteAlert.AlertLevel level, String message, Map<String, Object> metadata) {
        SatelliteAlert alert = SatelliteAlert.builder()
                .alertId("ALERT-" + System.currentTimeMillis())
                .satelliteId(satelliteId)
                .alertLevel(level)
                .message(message)
                .timestamp(Instant.now())
                .metadata(metadata)
                .acknowledged(false)
                .category("TELEMETRY")
                .subsystem("MONITORING")
                .build();

        kafkaTemplate.send(ALERTS_TOPIC, satelliteId, alert);
        log.info("🚨 {} alert streamed for {}: {}", level, satelliteId, message);
    }

    /**
     * Stream mission updates
     */
    public void streamMissionUpdate(String missionId, MissionUpdate.MissionStatus status,
                                    String details, double completionPercentage) {
        MissionUpdate update = MissionUpdate.builder()
                .missionId(missionId)
                .missionName("Mission-" + missionId)
                .status(status)
                .details(details)
                .completionPercentage(completionPercentage)
                .timestamp(Instant.now())
                .currentPhase("Execution")
                .build();

        kafkaTemplate.send(MISSION_UPDATES_TOPIC, missionId, update);
        log.info("📋 Mission update streamed for {}: {}% complete", missionId, completionPercentage);
    }

    /**
     * Listen to incoming commands
     */
    @KafkaListener(topics = COMMANDS_TOPIC, groupId = "mission-control")
    public void handleIncomingCommand(SatelliteCommand command) {
        log.info("⚡ Received command for {}: {}", command.getSatelliteId(), command.getCommandType());

        // Process command and send result
        CommandResult result = processCommand(command);

        kafkaTemplate.send("command-results", command.getSatelliteId(), result);
    }

    // Helper methods
    private PositionData generateOrbitPosition() {
        return PositionData.builder()
                .x(-4000 + Math.random() * 8000) // LEO orbit range
                .y(-4000 + Math.random() * 8000)
                .z(-2000 + Math.random() * 4000)
                .build();
    }

    private VelocityData generateOrbitVelocity() {
        return VelocityData.builder()
                .vx(-4 + Math.random() * 8) // km/s
                .vy(-4 + Math.random() * 8)
                .vz(-2 + Math.random() * 4)
                .build();
    }

    private AttitudeData generateAttitudeData() {
        return AttitudeData.builder()
                .roll(Math.random() * 360)
                .pitch(Math.random() * 360)
                .yaw(Math.random() * 360)
                .build();
    }

    private SystemStatus generateSystemStatus() {
        return SystemStatus.builder()
                .communicationSystem(Math.random() > 0.05) // 95% uptime
                .powerSystem(Math.random() > 0.02) // 98% uptime
                .propulsionSystem(Math.random() > 0.1) // 90% uptime
                .payloadSystem(Math.random() > 0.08) // 92% uptime
                .attitudeControlSystem(Math.random() > 0.03) // 97% uptime
                .build();
    }

    private CommandResult processCommand(SatelliteCommand command) {
        // Simulate command processing
        boolean success = Math.random() > 0.05; // 95% success rate

        return CommandResult.builder()
                .commandId(command.getCommandId())
                .satelliteId(command.getSatelliteId())
                .status(success ? CommandResult.CommandStatus.COMPLETED : CommandResult.CommandStatus.FAILED)
                .result(success ? "Command executed successfully" : "Command execution failed")
                .executionTime(System.currentTimeMillis())
                .startTime(Instant.now().minusSeconds(2))
                .endTime(Instant.now())
                .successProbability(success ? 1.0 : 0.0)
                .build();
    }
}
