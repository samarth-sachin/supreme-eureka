package com.dsl.simulator.Service;

import com.dsl.simulator.dto.SatelliteInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class N2YOService {

    @Value("${n2yo.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public SatelliteInfoDTO getSatelliteInfo(int noradId) {
        try {
            // N2YO API endpoint
            String url = String.format(
                    "https://api.n2yo.com/rest/v1/satellite/positions/%d/0/0/0/1/&apiKey=%s",
                    noradId, apiKey
            );

            log.info("Fetching satellite info from N2YO for NORAD: {}", noradId);

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("info")) {
                return parseSatelliteInfo(response, noradId);
            }

        } catch (Exception e) {
            log.error("Error fetching from N2YO API: {}", e.getMessage());
        }

        // Fallback to basic info
        return getDefaultSatelliteInfo(noradId);
    }

    private SatelliteInfoDTO parseSatelliteInfo(JsonNode response, int noradId) {
        JsonNode info = response.get("info");
        String name = info.get("satname").asText();

        // Get position data
        JsonNode position = response.get("positions").get(0);
        double altitude = position.get("sataltitude").asDouble();
        double latitude = position.get("satlatitude").asDouble();
        double longitude = position.get("satlongitude").asDouble();

        // Determine orbit type
        String orbitType = determineOrbitType(altitude);

        // Infer satellite type and purpose
        String type = inferSatelliteType(name);
        String purpose = inferPurpose(name, type);
        String icon = getIcon(type);

        // Generate educational content
        List<String> funFacts = generateFunFacts(name, type, altitude, orbitType);
        String missionDescription = generateMissionDescription(type, name);

        return SatelliteInfoDTO.builder()
                .name(name)
                .noradId(noradId)
                .type(type)
                .country(inferCountry(noradId, name))
                .orbitType(orbitType)
                .altitudeKm(altitude)
                .purpose(purpose)
                .missionDescription(missionDescription)
                .funFacts(funFacts)
                .icon(icon)
                .latitude(latitude)
                .longitude(longitude)
                .velocity(calculateOrbitalVelocity(altitude))
                .build();
    }

    private String determineOrbitType(double altitude) {
        if (altitude < 2000) {
            return "LEO (Low Earth Orbit)";
        } else if (altitude >= 2000 && altitude < 35786) {
            return "MEO (Medium Earth Orbit)";
        } else if (altitude >= 35700 && altitude <= 35900) {
            return "GEO (Geostationary Orbit)";
        } else {
            return "HEO (High Earth Orbit)";
        }
    }

    private String inferSatelliteType(String name) {
        String nameLower = name.toLowerCase();

        if (nameLower.contains("iss") || nameLower.contains("station")) return "Space Station";
        if (nameLower.contains("noaa") || nameLower.contains("weather")) return "Weather";
        if (nameLower.contains("gps") || nameLower.contains("navstar")) return "Navigation";
        if (nameLower.contains("hubble") || nameLower.contains("telescope")) return "Space Telescope";
        if (nameLower.contains("starlink") || nameLower.contains("oneweb")) return "Communication";
        if (nameLower.contains("spy") || nameLower.contains("nro")) return "Reconnaissance";
        if (nameLower.contains("iridium") || nameLower.contains("globalstar")) return "Communication";
        if (nameLower.contains("landsat") || nameLower.contains("terra")) return "Earth Observation";

        return "Scientific";
    }

    private String inferPurpose(String name, String type) {
        switch (type) {
            case "Weather": return "Weather forecasting and climate monitoring";
            case "Navigation": return "Global positioning and navigation services";
            case "Space Station": return "Human spaceflight and scientific research";
            case "Space Telescope": return "Astronomical observations and deep space imaging";
            case "Communication": return "Global internet and telecommunications";
            case "Earth Observation": return "Earth imaging and environmental monitoring";
            case "Reconnaissance": return "Intelligence gathering and surveillance";
            default: return "Scientific research and space exploration";
        }
    }

    private String getIcon(String type) {
        switch (type) {
            case "Space Station": return "🛰️";
            case "Weather": return "🌤️";
            case "Navigation": return "📍";
            case "Space Telescope": return "🔭";
            case "Communication": return "📡";
            case "Earth Observation": return "🌍";
            default: return "🛰️";
        }
    }

    private List<String> generateFunFacts(String name, String type, double altitude, String orbitType) {
        List<String> facts = new ArrayList<>();

        // Altitude-based facts
        if (altitude < 500) {
            facts.add("🌍 Flies very close to Earth - experiences atmospheric drag");
        } else if (altitude > 35000) {
            facts.add("🌌 Orbits at same speed as Earth's rotation (appears stationary)");
        }

        // Velocity calculation
        double velocity = calculateOrbitalVelocity(altitude);
        facts.add(String.format("⚡ Travels at %.2f km/s (%.0f km/h)", velocity, velocity * 3600));

        // Orbital period
        double period = calculateOrbitalPeriod(altitude);
        if (period < 2) {
            facts.add(String.format("🔄 Completes one orbit in %.0f minutes", period * 60));
        } else if (period < 24) {
            facts.add(String.format("🔄 Completes %.1f orbits per day", 24.0 / period));
        } else {
            facts.add("🔄 Takes 24 hours to complete one orbit (geostationary)");
        }

        // Type-specific facts
        switch (type) {
            case "Space Station":
                facts.add("👨‍🚀 Home to astronauts conducting microgravity experiments");
                break;
            case "Weather":
                facts.add("🌦️ Provides crucial data for hurricane and storm tracking");
                break;
            case "Navigation":
                facts.add("📱 Used by billions of devices for GPS navigation");
                break;
            case "Communication":
                facts.add("📶 Provides internet access to remote areas");
                break;
        }

        return facts;
    }

    private String generateMissionDescription(String type, String name) {
        switch (type) {
            case "Space Station":
                return "Serves as a microgravity laboratory where scientific research is conducted in astrobiology, astronomy, meteorology, and physics. Enables long-duration human spaceflight missions.";
            case "Weather":
                return "Monitors Earth's weather patterns, ocean temperatures, and atmospheric conditions. Data is used for weather forecasting, climate research, and disaster management.";
            case "Navigation":
                return "Part of the Global Positioning System (GPS) constellation. Provides precise location and time information for military and civilian users worldwide.";
            case "Space Telescope":
                return "Observes distant galaxies, nebulae, and celestial phenomena. Free from atmospheric distortion, enabling unprecedented views of the universe.";
            case "Communication":
                return "Provides global internet connectivity and telecommunications services. Part of next-generation satellite internet constellation.";
            case "Earth Observation":
                return "Captures high-resolution images of Earth's surface for environmental monitoring, urban planning, agriculture, and disaster response.";
            default:
                return "Conducts scientific research and space exploration missions. Contributes to our understanding of space and Earth systems.";
        }
    }

    private String inferCountry(int noradId, String name) {
        // NORAD ID ranges (approximate)
        if (noradId < 10000) return "USA";
        if (noradId >= 40000 && noradId < 45000) return "Russia/USSR";
        if (noradId >= 28000 && noradId < 29000) return "China";

        // Name-based inference
        String nameLower = name.toLowerCase();
        if (nameLower.contains("noaa") || nameLower.contains("nasa")) return "USA";
        if (nameLower.contains("cosmos") || nameLower.contains("molniya")) return "Russia";
        if (nameLower.contains("beidou") || nameLower.contains("yaogan")) return "China";
        if (nameLower.contains("sentinel") || nameLower.contains("esa")) return "Europe";
        if (nameLower.contains("asnaro") || nameLower.contains("jaxa")) return "Japan";
        if (nameLower.contains("cartosat") || nameLower.contains("isro")) return "India";

        return "International";
    }

    private double calculateOrbitalVelocity(double altitudeKm) {
        double earthRadius = 6371.0; // km
        double G = 6.67430e-11; // gravitational constant
        double M = 5.972e24; // Earth's mass in kg
        double r = (earthRadius + altitudeKm) * 1000; // convert to meters

        double velocity = Math.sqrt(G * M / r) / 1000; // km/s
        return velocity;
    }

    private double calculateOrbitalPeriod(double altitudeKm) {
        double earthRadius = 6371.0; // km
        double r = earthRadius + altitudeKm;
        double period = 2 * Math.PI * Math.sqrt(Math.pow(r, 3) / 398600.4418); // hours
        return period;
    }

    private SatelliteInfoDTO getDefaultSatelliteInfo(int noradId) {
        return SatelliteInfoDTO.builder()
                .name("Satellite " + noradId)
                .noradId(noradId)
                .type("Unknown")
                .country("Unknown")
                .orbitType("LEO (Low Earth Orbit)")
                .altitudeKm(500.0)
                .purpose("Data not available from N2YO API")
                .missionDescription("Satellite information could not be retrieved. This may be a classified or recently launched satellite.")
                .funFacts(List.of("📡 Real-time TLE data available", "🌍 Orbiting Earth"))
                .icon("🛰️")
                .build();
    }
}
