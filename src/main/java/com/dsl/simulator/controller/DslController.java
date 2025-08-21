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

//package com.dsl.simulator.controller;
//
//import com.dsl.simulator.Product.Orbit;
//import com.dsl.simulator.SatOpsLexer;
//import com.dsl.simulator.SatOpsParser;
//import com.dsl.simulator.visitor.SatOpsVisitor;
//import com.dsl.simulator.Orekit.SatellitePropagation;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/dsl")
//public class DslController {
//
//    @PostMapping("/run")
//    public String runDsl(@RequestBody String script) {
//        try {
//            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromString(script));
//            SatOpsParser parser = new SatOpsParser(new CommonTokenStream(lexer));
//
//            SatOpsVisitor visitor = new SatOpsVisitor();
//            visitor.visitProgram(parser.program());
//
//
//
////            SatellitePropagation sim = new SatellitePropagation();
////            sim.simulateOrbit(7000e3, 0.001, 98.7);
//            Orbit orbit = visitor.getCurrentOrbit();
//
//
//            // Print visitor results on terminal
//            System.out.println("Visitor Satellites: " + visitor.getSatellites().values());
//            System.out.println("Visitor GroundStations: " + visitor.getGroundStations().values());
//
//            return " DSL executed successfully!\n\n" +
//                    " Satellites: " + visitor.getSatellites().values() + "\n" +
//                    " GroundStations: " + visitor.getGroundStations().values()+"\n"
//                    +"Future Velocity:"+visitor.getLastVelocity();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return " Error executing DSL: " + e.getMessage();
//        }
//    }
//}
//



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
