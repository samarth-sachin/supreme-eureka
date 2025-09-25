package com.dsl.simulator.controller;

import com.dsl.simulator.RealAI.*;
import com.dsl.simulator.Runner.SatOpsRunner;
import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.ConstellationOptimizer;
import com.dsl.simulator.Streaming.TelemetryStreamer;
import com.dsl.simulator.error.DescriptiveErrorListener;
import com.dsl.simulator.Service.MissionControlService;
import com.dsl.simulator.Service.SatOpsVisitor;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dsl")
@RequiredArgsConstructor
public class DslController {

    private final MissionControlService missionControlService;
    private final TelemetryStreamer telemetryStreamer;
    private final SatOpsRunner runner;
    private final ConstellationOptimizer optimizer;
    private final RealAIService realAIService; // NEW
    private final SatelliteHealthPredictor healthPredictor; // NEW
    private final AnomalyDetectionNetwork anomalyDetector; // NEW
    private final PatternRecognitionLSTM patternAnalyzer; // NEW
    private final CollisionRiskClassifier collisionClassifier;

    @PostMapping("/run")
    public String runDSL(@RequestBody String script) {
        try {
            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(script));
            SatOpsParser parser = new SatOpsParser(new CommonTokenStream(lexer));

            parser.removeErrorListeners();
            DescriptiveErrorListener errorListener = new DescriptiveErrorListener();
            parser.addErrorListener(errorListener);

            SatOpsParser.ProgramContext tree = parser.program();

            if (errorListener.hasErrors()) {
                return String.join("\n", errorListener.getErrorMessages());
            }

            SatOpsVisitor visitor = new SatOpsVisitor(missionControlService, telemetryStreamer, optimizer,realAIService,healthPredictor,anomalyDetector,patternAnalyzer,collisionClassifier);
            visitor.visit(tree);
            visitor.printLogs();

            return String.join("\n", visitor.getLogs());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/auto")
    public String autoDSL() {
        return runner.runFromResource("script.stx");
    }
}