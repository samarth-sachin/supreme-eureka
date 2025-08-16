package com.dsl.simulator.controller;

//import com.dsl.simulator.Orekit.OrekitInitializer;
import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.visitor.SatOpsVisitorImpl;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dsl")
public class DslController {

    @PostMapping(value = "/run", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String runDsl(@RequestBody String code) {

//        OrekitInitializer.initialize();

        SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SatOpsParser parser = new SatOpsParser(tokens);
        SatOpsVisitorImpl visitor = new SatOpsVisitorImpl();

        return visitor.visitProgram(parser.program());
    }
}
