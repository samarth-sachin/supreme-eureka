package com.dsl.simulator.Runner;

import com.dsl.simulator.RealAI.*;
import com.dsl.simulator.Repository.SatelliteRepository;
import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.ConstellationOptimizer;
import com.dsl.simulator.Service.MissionControlService;
import com.dsl.simulator.Service.SatOpsVisitor;
import com.dsl.simulator.Streaming.TelemetryStreamer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class SatOpsRunner {

    private final MissionControlService missionControlService;
    private final TelemetryStreamer telemetryStreamer; // CAN BE NULL
    private final ConstellationOptimizer optimizer;
    private final RealAIService realAIService;
    private final SatelliteHealthPredictor healthPredictor;
    private final AnomalyDetectionNetwork anomalyDetector;
    private final PatternRecognitionLSTM patternAnalyzer;
    private final CollisionRiskClassifier collisionClassifier;
    private final SatelliteRepository satelliteRepository;

    @Autowired
    public SatOpsRunner(
            MissionControlService missionControlService,
            @Autowired(required = false) TelemetryStreamer telemetryStreamer, // ✅ OPTIONAL
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
        this.optimizer = optimizer;
        this.realAIService = realAIService;
        this.healthPredictor = healthPredictor;
        this.anomalyDetector = anomalyDetector;
        this.patternAnalyzer = patternAnalyzer;
        this.collisionClassifier = collisionClassifier;
        this.satelliteRepository = satelliteRepository;
    }

    public String runFromResource(String filename) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
            if (inputStream == null) {
                return "Error: Resource file not found: " + filename;
            }

            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromStream(inputStream, StandardCharsets.UTF_8));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SatOpsParser parser = new SatOpsParser(tokens);
            SatOpsParser.ProgramContext tree = parser.program();

            SatOpsVisitor visitor = new SatOpsVisitor(
                    missionControlService,
                    telemetryStreamer, // Can be null - visitor handles it
                    optimizer,
                    realAIService,
                    healthPredictor,
                    anomalyDetector,
                    patternAnalyzer,
                    collisionClassifier,
                    satelliteRepository
            );
            visitor.visit(tree);

            return String.join("\n", visitor.getLogs());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run DSL from resource: " + e.getMessage(), e);
        }
    }
}
