package com.dsl.simulator.Runner;

import com.dsl.simulator.SatOpsLexer;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.visitor.SatOpsVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.InputStream;

public class SatOpsRunner {

    public String runFromResource(String filename) {
        try {

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
            if (inputStream == null) {
                throw new RuntimeException("File not found in resources: " + filename);
            }


            SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromStream(inputStream));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SatOpsParser parser = new SatOpsParser(tokens);


            SatOpsParser.ProgramContext tree = parser.program();


            SatOpsVisitor visitor = new SatOpsVisitor();
            visitor.visit(tree);


            return String.join("\n", visitor.getLogs());

        } catch (Exception e) {
            throw new RuntimeException("Failed to run DSL: " + e.getMessage(), e);
        }
    }
}
