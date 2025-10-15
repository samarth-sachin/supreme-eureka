package com.dsl.simulator.controller;

import com.dsl.simulator.RealAI.*;
import com.dsl.simulator.Repository.SatelliteRepository;
import com.dsl.simulator.Runner.SatOpsRunner;
import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.ConstellationOptimizer;
import com.dsl.simulator.Streaming.TelemetryStreamer;
import com.dsl.simulator.error.DescriptiveErrorListener;
import com.dsl.simulator.Service.MissionControlService;
import com.dsl.simulator.Service.SatOpsVisitor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dsl")
@CrossOrigin(origins = "*", allowedHeaders = "*")  // ✅ Enable CORS for this controller
public class DslController {

    private final MissionControlService missionControlService;
    private final TelemetryStreamer telemetryStreamer;
    private final SatOpsRunner runner;
    private final ConstellationOptimizer optimizer;
    private final RealAIService realAIService;
    private final SatelliteHealthPredictor healthPredictor;
    private final AnomalyDetectionNetwork anomalyDetector;
    private final PatternRecognitionLSTM patternAnalyzer;
    private final CollisionRiskClassifier collisionClassifier;
    private final SatelliteRepository satelliteRepository;

    @Autowired
    public DslController(
            MissionControlService missionControlService,
            @Autowired(required = false) TelemetryStreamer telemetryStreamer,
            SatOpsRunner runner,
            ConstellationOptimizer optimizer,
            RealAIService realAIService,
            SatelliteHealthPredictor healthPredictor,
            AnomalyDetectionNetwork anomalyDetector,
            PatternRecognitionLSTM patternAnalyzer,
            CollisionRiskClassifier collisionClassifier,
            SatelliteRepository satelliteRepository
    ) {
        this.missionControlService = missionControlService;
        this.telemetryStreamer = telemetryStreamer;
        this.runner = runner;
        this.optimizer = optimizer;
        this.realAIService = realAIService;
        this.healthPredictor = healthPredictor;
        this.anomalyDetector = anomalyDetector;
        this.patternAnalyzer = patternAnalyzer;
        this.collisionClassifier = collisionClassifier;
        this.satelliteRepository = satelliteRepository;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runDSL(@RequestBody String script) {
        try {
            log.info("Received DSL command: {}", script);

            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(script));
            SatOpsParser parser = new SatOpsParser(new CommonTokenStream(lexer));

            parser.removeErrorListeners();
            DescriptiveErrorListener errorListener = new DescriptiveErrorListener();
            parser.addErrorListener(errorListener);

            SatOpsParser.ProgramContext tree = parser.program();

            if (errorListener.hasErrors()) {
                String errors = String.join("\n", errorListener.getErrorMessages());
                log.error("DSL parsing errors: {}", errors);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "text/plain; charset=UTF-8")
                        .body(errors);
            }

            SatOpsVisitor visitor = new SatOpsVisitor(
                    missionControlService,
                    telemetryStreamer,
                    optimizer,
                    realAIService,
                    healthPredictor,
                    anomalyDetector,
                    patternAnalyzer,
                    collisionClassifier,
                    satelliteRepository
            );

            visitor.visit(tree);
            visitor.printLogs();

            String result = String.join("\n", visitor.getLogs());
            log.info("DSL execution completed successfully");

            // ✅ Return with proper headers
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(result);

        } catch (Exception e) {
            log.error("Error executing DSL: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/auto")
    public ResponseEntity<String> autoDSL() {
        try {
            log.info("Running auto DSL script");
            String result = runner.runFromResource("script.stx");

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(result);

        } catch (Exception e) {
            log.error("Error running auto DSL: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // ✅ OPTIONS endpoint for CORS preflight
    @RequestMapping(value = "/run", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity
                .ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .build();
    }
}
