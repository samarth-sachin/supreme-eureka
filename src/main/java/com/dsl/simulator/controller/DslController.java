package com.dsl.simulator.controller;

import com.dsl.simulator.Runner.SatOpsRunner;
import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.MissionControlService;
import com.dsl.simulator.Service.SatOpsVisitor; // Corrected import path
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dsl")
@RequiredArgsConstructor
public class DslController {

    private final MissionControlService missionControlService;

    // THE FIX: Add the 'final' keyword here.
    private final SatOpsRunner runner;

    @PostMapping("/run")
    public String runDSL(@RequestBody String script) {
        try {
            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(script));
            SatOpsParser parser = new SatOpsParser(new CommonTokenStream(lexer));

            SatOpsVisitor visitor = new SatOpsVisitor(missionControlService);
            visitor.visit(parser.program());

            return String.join("\n", visitor.getLogs());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/auto")
    public String autoDSL() {
        // This will now work because 'runner' is properly injected.
        return runner.runFromResource("script.stx");
    }
}