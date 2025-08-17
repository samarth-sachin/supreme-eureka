package com.dsl.simulator.controller;

import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.visitor.SatOpsVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dsl")
public class DslController {

    @PostMapping("/run")
    public String runDsl(@RequestBody String script) {
        try {
            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(script));
            SatOpsParser parser = new SatOpsParser(new CommonTokenStream(lexer));

            SatOpsVisitor visitor = new SatOpsVisitor();
            visitor.visitProgram(parser.program());


            return " DSL executed successfully!\n\n" +
                    " Satellites: " + visitor.getSatellites().values() + "\n" +
                    " GroundStations: " + visitor.getGroundStations().values();
        } catch (Exception e) {
            return " Error executing DSL: " + e.getMessage();
        }
    }
}


//
//package com.dsl.simulator.controller;
//
//import com.dsl.simulator.SatOpsLexer;
//import com.dsl.simulator.SatOpsParser;
//import com.dsl.simulator.visitor.SatOpsCustomVisitor;
//import com.dsl.simulator.visitor.SatOpsVisitor;
//import org.antlr.v4.runtime.CharStream;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/dsl")
//public class DslController {
//
//    @PostMapping("/execute")
//    public String executeDsl(@RequestBody String dslCode) {
//        try {
////           Convert DSL code into a CharStream
//            CharStream input = CharStreams.fromString(dslCode);
//
//            //  Run lexer
//            SatOpsLexer lexer = new SatOpsLexer(input);
//            CommonTokenStream tokens = new CommonTokenStream(lexer);
//
//            //  Run parser
//            SatOpsParser parser = new SatOpsParser(tokens);
//
//            // Step 4: Use our custom visitor (connected to CommandExecutor → SatOpsExecutor → Orekit)
////            SatOpsCustomVisitor visitor = new SatOpsCustomVisitor();
//            SatOpsVisitor visitor = new SatOpsVisitor();
//
//            visitor.visitProgram(parser.program());
//
//            return "DSL Execution finished successfully!";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return " Error while executing DSL: " + e.getMessage();
//        }
//    }
//}
