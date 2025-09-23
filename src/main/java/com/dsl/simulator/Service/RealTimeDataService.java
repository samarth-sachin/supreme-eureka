package com.dsl.simulator.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class RealTimeDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🛰️ GET REAL ISS TELEMETRY
    public JsonNode getISSTelemetry() {
        try {
            String url = "http://api.open-notify.org/iss-now.json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Failed to get ISS telemetry: " + e.getMessage());
        }
        return null; // Will trigger fallback in calling service
    }

    // 🌞 GET REAL SPACE WEATHER DATA
    public JsonNode getSpaceWeatherData() {
        try {
            // NOAA Space Weather API - Real Kp index data
            String url = "https://services.swpc.noaa.gov/json/planetary_k_index_1m.json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Failed to get space weather data: " + e.getMessage());
            // Try backup API
            return getBackupSpaceWeatherData();
        }
        return null;
    }

    private JsonNode getBackupSpaceWeatherData() {
        try {
            // Alternative space weather source
            String url = "https://services.swpc.noaa.gov/json/planetary_k_index.json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Backup space weather API also failed: " + e.getMessage());
        }
        return null;
    }

    // 🌍 CALCULATE REAL-TIME ATMOSPHERIC DENSITY
    public double getRealTimeAtmosphericDensity(double altitudeMeters) {
        try {
            // Get space weather conditions to adjust density
            JsonNode spaceWeather = getSpaceWeatherData();

            double kpIndex = 3.0; // Default
            if (spaceWeather != null && spaceWeather.isArray() && spaceWeather.size() > 0) {
                kpIndex = spaceWeather.get(spaceWeather.size() - 1).path("kp_index").asDouble(3.0);
            }

            // Base density using standard atmospheric model
            double altitudeKm = altitudeMeters / 1000.0;
            double baseDensity = calculateStandardDensity(altitudeKm);

            // Adjust for space weather (geomagnetic activity increases heating -> expansion -> lower density at same altitude)
            double spaceWeatherFactor = 1.0 + (kpIndex - 3.0) * 0.15; // 15% change per Kp unit

            // Add random variation for realism (±10%)
            double randomFactor = 0.9 + Math.random() * 0.2;

            return baseDensity * spaceWeatherFactor * randomFactor;

        } catch (Exception e) {
            // Fallback to simple model
            return calculateStandardDensity(altitudeMeters / 1000.0);
        }
    }

    private double calculateStandardDensity(double altitudeKm) {
        // Standard atmospheric density model (simplified)
        if (altitudeKm < 200) {
            return 1.225 * Math.exp(-altitudeKm / 8.5); // Exponential decay
        } else if (altitudeKm < 500) {
            // Thermosphere region - steeper decay
            return 2.6e-10 * Math.exp(-(altitudeKm - 200) / 63.0);
        } else {
            // Exosphere - very low density
            return 1e-15 * Math.exp(-(altitudeKm - 500) / 200.0);
        }
    }

    // 🛰️ GET SATELLITE PASS PREDICTIONS (Future enhancement)
    public JsonNode getSatellitePassPredictions(double latitude, double longitude) {
        try {
            // N2YO API for satellite pass predictions (requires API key)
            String url = String.format("https://api.n2yo.com/rest/v1/satellite/passes/25544/%.4f/%.4f/0/2/40/&apiKey=YOUR_API_KEY", latitude, longitude);
            // This would require API key registration

            // For now, return null to use internal calculations
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // 🌐 HEALTH CHECK for all APIs
    public String checkApiHealth() {
        StringBuilder health = new StringBuilder();
        health.append("🔍 === API HEALTH CHECK ===\n");

        // Check ISS API
        JsonNode issData = getISSTelemetry();
        health.append("ISS Telemetry API: ").append(issData != null ? "✅ ONLINE" : "❌ OFFLINE").append("\n");

        // Check Space Weather API
        JsonNode weatherData = getSpaceWeatherData();
        health.append("Space Weather API: ").append(weatherData != null ? "✅ ONLINE" : "❌ OFFLINE").append("\n");

        health.append("========================");
        return health.toString();
    }
}
