package com.dsl.simulator.controller;

import com.dsl.simulator.Entity.*;
import com.dsl.simulator.Repository.*;
import com.dsl.simulator.Service.MissionControlService;
import com.dsl.simulator.Service.N2YOService;
import com.dsl.simulator.dto.SatelliteInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/frontend")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FrontendApiController {

    private final SatelliteRepository satelliteRepository;
    private final GroundStationRepository groundStationRepository;
    private final MessageRepository messageRepository;
    private final CommandHistoryRepository commandHistoryRepository;
    private final MissionControlService missionControlService;

    @GetMapping("/satellites")
    public ResponseEntity<List<Map<String, Object>>> getAllSatellites() {
        List<Satellite> satellites = satelliteRepository.findAll();

        List<Map<String, Object>> response = satellites.stream().map(sat -> {
            Map<String, Object> satMap = new HashMap<>();

            // ✅ IMPORTANT: Use the EXACT field names frontend expects
            satMap.put("satelliteId", sat.getSatelliteId());  // ✅ Frontend uses this
            satMap.put("id", sat.getSatelliteId());           // ✅ Backup
            satMap.put("name", sat.getName());
            satMap.put("noradId", sat.getNoradId());
            satMap.put("tleLine1", sat.getTleLine1());
            satMap.put("tleLine2", sat.getTleLine2());
            satMap.put("latitude", sat.getLatitude());
            satMap.put("longitude", sat.getLongitude());
            satMap.put("altitude", sat.getAltitude());
            satMap.put("status", sat.getStatus());

            return satMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deploy-satellite")
    public ResponseEntity<Map<String, Object>> deploySatellite(@RequestBody Map<String, Object> request) {
        try {
            String satelliteId = (String) request.get("satelliteId");
            Integer noradId = (Integer) request.get("noradId");

            if (satelliteId == null || noradId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing: satelliteId and noradId"));
            }

            if (satelliteRepository.existsBySatelliteId(satelliteId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Satellite already deployed"));
            }

            com.dsl.simulator.Product.Satellite deployedSat =
                    missionControlService.deploySatellite(satelliteId, noradId);

            Satellite dbSatellite = Satellite.builder()
                    .satelliteId(satelliteId)
                    .name(deployedSat.getSatelliteName())
                    .noradId(noradId)
                    .tleLine1("CELESTRAK_DATA")
                    .tleLine2("CELESTRAK_DATA")
                    .status("OPERATIONAL")
                    .build();

            satelliteRepository.save(dbSatellite);

            commandHistoryRepository.save(CommandHistory.builder()
                    .command("deploy " + satelliteId + " with id " + noradId)
                    .result("Success")
                    .status("SUCCESS")
                    .build());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "satelliteId", satelliteId,
                    "name", deployedSat.getSatelliteName(),
                    "noradId", noradId
            ));

        } catch (Exception e) {
            log.error("Deploy error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/deploy-groundstation")
    public ResponseEntity<Map<String, Object>> deployGroundStation(@RequestBody Map<String, Object> request) {
        try {
            String stationId = (String) request.get("stationId");
            Double lat = ((Number) request.get("latitude")).doubleValue();
            Double lon = ((Number) request.get("longitude")).doubleValue();

            GroundStation gs = GroundStation.builder()
                    .stationId(stationId)
                    .name(stationId)
                    .latitude(lat)
                    .longitude(lon)
                    .build();

            groundStationRepository.save(gs);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            Message msg = Message.builder()
                    .fromEntity((String) request.get("from"))
                    .toEntity((String) request.get("to"))
                    .messageText((String) request.get("message"))
                    .build();

            messageRepository.save(msg);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/command-history")
    public ResponseEntity<List<CommandHistory>> getCommandHistory() {
        return ResponseEntity.ok(commandHistoryRepository.findTop20ByOrderByExecutedAtDesc());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "satellites", satelliteRepository.count()
        ));
    }
    @Autowired
    private N2YOService n2yoService;

    @GetMapping("/satellite-info/{noradId}")
    public ResponseEntity<SatelliteInfoDTO> getSatelliteInfo(@PathVariable Integer noradId) {
        SatelliteInfoDTO info = n2yoService.getSatelliteInfo(noradId);
        return ResponseEntity.ok(info);
    }
}
