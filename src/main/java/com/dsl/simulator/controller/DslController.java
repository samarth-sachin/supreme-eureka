package com.dsl.simulator.controller;

import com.dsl.simulator.Runner.SatOpsRunner;
import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.visitor.SatOpsVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dsl")
public class DslController {
 private final SatOpsRunner runner=new SatOpsRunner();
    @PostMapping("/run")
    public String runDSL(@RequestBody String script) {
        try {
            // ANTLR4
            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(script));
            SatOpsParser parser = new SatOpsParser(new CommonTokenStream(lexer));

            // all dsl here
            SatOpsVisitor visitor = new SatOpsVisitor();
            visitor.visit(parser.program());

            // return done work in visitor
            return String.join("\n", visitor.getLogs());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error while running DSL: " + e.getMessage();
        }
    }
    @GetMapping("/auto")
    public String autoDSL() {
        return runner.runFromResource("script.stx");
    }
}

