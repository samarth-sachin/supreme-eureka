package com.dsl.simulator.Runner;

import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.ConstellationOptimizer;
import com.dsl.simulator.Service.MissionControlService; // Correct import for the service
import com.dsl.simulator.Service.SatOpsVisitor;       // Correct import for the visitor
import com.dsl.simulator.Streaming.TelemetryStreamer;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class SatOpsRunner {

    // 3. Define the required service dependency
    private final MissionControlService missionControlService;
    private final TelemetryStreamer telemetryStreamer;
    private final ConstellationOptimizer constellationOptimizer;

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


            SatOpsVisitor visitor = new SatOpsVisitor(missionControlService,telemetryStreamer,constellationOptimizer);
            visitor.visit(tree);

            return String.join("\n", visitor.getLogs());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run DSL from resource: " + e.getMessage(), e);
        }
    }
}