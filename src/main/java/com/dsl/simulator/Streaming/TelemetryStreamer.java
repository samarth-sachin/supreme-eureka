package com.dsl.simulator.Streaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class TelemetryStreamer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, Boolean> activeStreams = new ConcurrentHashMap<>();

    // Kafka Topics
    private static final String TELEMETRY_TOPIC = "satellite-telemetry";
    private static final String ALERTS_TOPIC = "satellite-alerts";
    private static final String COMMANDS_TOPIC = "satellite-commands";
    private static final String MISSION_UPDATES_TOPIC = "mission-updates";

    // Manual constructor with optional KafkaTemplate
    @Autowired(required = false)
    public TelemetryStreamer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Start real-time telemetry streaming for a satellite
     */
    public String startTelemetryStream(String satelliteId) {
        if (kafkaTemplate == null) {
            return "❌ Kafka not enabled";
        }

        if (activeStreams.getOrDefault(satelliteId, false)) {
            return "⚠️ Telemetry stream already active for " + satelliteId;
        }

        activeStreams.put(satelliteId, true);
        log.info("📡 Starting telemetry stream for {}", satelliteId);

        scheduler.scheduleAtFixedRate(() -> {
            if (activeStreams.getOrDefault(satelliteId, false)) {
                streamTelemetryData(satelliteId);
            }
        }, 0, 5, TimeUnit.SECONDS);

        return "✅ Real-time telemetry streaming started for " + satelliteId;
    }

    public String stopTelemetryStream(String satelliteId) {
        activeStreams.put(satelliteId, false);
        log.info("🛑 Stopping telemetry stream for {}", satelliteId);
        return "✅ Telemetry streaming stopped for " + satelliteId;
    }

    private void streamTelemetryData(String satelliteId) {
        if (kafkaTemplate == null) return;

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

            checkAndStreamAlerts(satelliteId, telemetry);

        } catch (Exception e) {
            log.error("❌ Error streaming telemetry for {}: {}", satelliteId, e.getMessage());
        }
    }

    private TelemetryData generateRealisticTelemetry(String satelliteId) {
        return TelemetryData.builder()
                .satelliteId(satelliteId)
                .timestamp(Instant.now())
                .batteryLevel(85.0 + Math.random() * 10 - 5)
                .powerGeneration(200 + Math.random() * 100)
                .fuelLevel(70.0 + Math.random() * 20)
                .temperature(20 + Math.random() * 30 - 15)
                .position(generateOrbitPosition())
                .velocity(generateOrbitVelocity())
                .attitude(generateAttitudeData())
                .systemStatus(generateSystemStatus())
                .dataRate(1000 + Math.random() * 5000)
                .signalStrength(-65 - Math.random() * 20)
                .build();
    }

    private void checkAndStreamAlerts(String satelliteId, TelemetryData telemetry) {
        if (kafkaTemplate == null) return;

        if (telemetry.getBatteryLevel() < 20) {
            streamAlert(satelliteId, SatelliteAlert.AlertLevel.CRITICAL,
                    "Battery level critically low: " + String.format("%.1f%%", telemetry.getBatteryLevel()),
                    Map.of("batteryLevel", telemetry.getBatteryLevel()));
        }

        if (telemetry.getTemperature() > 60 || telemetry.getTemperature() < -20) {
            streamAlert(satelliteId, SatelliteAlert.AlertLevel.WARNING,
                    "Temperature out of normal range: " + String.format("%.1f°C", telemetry.getTemperature()),
                    Map.of("temperature", telemetry.getTemperature()));
        }
    }

    public void streamAlert(String satelliteId, SatelliteAlert.AlertLevel level, String message, Map<String, Object> metadata) {
        if (kafkaTemplate == null) return;

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

    public void streamMissionUpdate(String missionId, MissionUpdate.MissionStatus status,
                                    String details, double completionPercentage) {
        if (kafkaTemplate == null) return;

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

    @KafkaListener(topics = COMMANDS_TOPIC, groupId = "mission-control")
    public void handleIncomingCommand(SatelliteCommand command) {
        if (kafkaTemplate == null) return;

        log.info("⚡ Received command for {}: {}", command.getSatelliteId(), command.getCommandType());
        CommandResult result = processCommand(command);
        kafkaTemplate.send("command-results", command.getSatelliteId(), result);
    }

    private PositionData generateOrbitPosition() {
        return PositionData.builder()
                .x(-4000 + Math.random() * 8000)
                .y(-4000 + Math.random() * 8000)
                .z(-2000 + Math.random() * 4000)
                .build();
    }

    private VelocityData generateOrbitVelocity() {
        return VelocityData.builder()
                .vx(-4 + Math.random() * 8)
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
                .communicationSystem(Math.random() > 0.05)
                .powerSystem(Math.random() > 0.02)
                .propulsionSystem(Math.random() > 0.1)
                .payloadSystem(Math.random() > 0.08)
                .attitudeControlSystem(Math.random() > 0.03)
                .build();
    }

    private CommandResult processCommand(SatelliteCommand command) {
        boolean success = Math.random() > 0.05;

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
