package com.dsl.simulator.AI;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class AnomalyDetectionService {

    /**
     * Detect anomalies in satellite telemetry using AI
     */
    public List<Anomaly> detectAnomalies(String satelliteId) {
        log.info("🔍 Running anomaly detection for {}", satelliteId);

        List<Anomaly> anomalies = new ArrayList<>();

        // Simulate various types of anomalies
        if (Math.random() > 0.6) { // 40% chance of battery anomaly
            anomalies.add(Anomaly.builder()
                    .satelliteId(satelliteId)
                    .type("POWER_ANOMALY")
                    .parameter("Battery Voltage")
                    .currentValue("28.2V")
                    .expectedValue("31.5V ±2V")
                    .confidence(0.87 + Math.random() * 0.12)
                    .severity(Math.random() > 0.5 ? "HIGH" : "MEDIUM")
                    .description("Battery voltage below expected range, possible degradation")
                    .detectionTime(Instant.now())
                    .likelyCause("Solar panel efficiency decrease or battery cell degradation")
                    .recommendation("Check solar panel orientation and battery health")
                    .build());
        }

        if (Math.random() > 0.8) { // 20% chance of thermal anomaly
            anomalies.add(Anomaly.builder()
                    .satelliteId(satelliteId)
                    .type("THERMAL_ANOMALY")
                    .parameter("CPU Temperature")
                    .currentValue("67°C")
                    .expectedValue("45°C ±10°C")
                    .confidence(0.92 + Math.random() * 0.07)
                    .severity("CRITICAL")
                    .description("CPU temperature exceeding safe operational limits")
                    .detectionTime(Instant.now())
                    .likelyCause("Thermal management system malfunction or high processing load")
                    .recommendation("Reduce processing load and check thermal control system")
                    .build());
        }

        if (Math.random() > 0.75) { // 25% chance of communication anomaly
            anomalies.add(Anomaly.builder()
                    .satelliteId(satelliteId)
                    .type("COMMUNICATION_ANOMALY")
                    .parameter("Signal Strength")
                    .currentValue("-98 dBm")
                    .expectedValue("-75 dBm ±10 dBm")
                    .confidence(0.79 + Math.random() * 0.15)
                    .severity("MEDIUM")
                    .description("Signal strength significantly below expected levels")
                    .detectionTime(Instant.now())
                    .likelyCause("Antenna misalignment or atmospheric interference")
                    .recommendation("Check antenna pointing and adjust for optimal signal")
                    .build());
        }

        log.info("🔍 Detected {} anomalies for {}", anomalies.size(), satelliteId);
        return anomalies;
    }

    /**
     * Real-time anomaly detection for critical alerts
     */
    public List<Anomaly> detectRealTimeAnomalies(String satelliteId) {
        log.info("⚡ Real-time anomaly detection for {}", satelliteId);

        List<Anomaly> criticalAnomalies = new ArrayList<>();

        // Only return critical anomalies that require immediate attention
        if (Math.random() > 0.85) { // 15% chance of critical anomaly
            criticalAnomalies.add(Anomaly.builder()
                    .satelliteId(satelliteId)
                    .type("CRITICAL_POWER_FAILURE")
                    .parameter("Main Power Bus")
                    .currentValue("18.5V")
                    .expectedValue("28.0V ±2V")
                    .confidence(0.96)
                    .severity("CRITICAL")
                    .description("Critical power system failure detected")
                    .detectionTime(Instant.now())
                    .likelyCause("Main power distribution failure")
                    .recommendation("Switch to backup power systems immediately")
                    .timeToAction(180) // 3 minutes
                    .build());
        }

        return criticalAnomalies;
    }

    /**
     * Analyze anomaly patterns over time
     */
    public AnomalyTrend analyzeAnomalyTrends(String satelliteId, int days) {
        log.info("📈 Analyzing anomaly trends for {} over {} days", satelliteId, days);

        Map<String, Integer> anomalyTypeCount = new HashMap<>();
        anomalyTypeCount.put("POWER_ANOMALY", (int)(Math.random() * 10));
        anomalyTypeCount.put("THERMAL_ANOMALY", (int)(Math.random() * 5));
        anomalyTypeCount.put("COMMUNICATION_ANOMALY", (int)(Math.random() * 8));
        anomalyTypeCount.put("ATTITUDE_ANOMALY", (int)(Math.random() * 3));

        int totalAnomalies = anomalyTypeCount.values().stream().mapToInt(Integer::intValue).sum();

        return AnomalyTrend.builder()
                .satelliteId(satelliteId)
                .analysisPeriodDays(days)
                .totalAnomalies(totalAnomalies)
                .anomaliesByType(anomalyTypeCount)
                .trendDirection(Math.random() > 0.5 ? "INCREASING" : "DECREASING")
                .averageAnomaliesPerDay(totalAnomalies / (double)days)
                .mostCommonAnomaly(Collections.max(anomalyTypeCount.entrySet(),
                        Map.Entry.comparingByValue()).getKey())
                .build();
    }
}
