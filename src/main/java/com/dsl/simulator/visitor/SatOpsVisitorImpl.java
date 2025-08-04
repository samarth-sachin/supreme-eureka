package com.dsl.simulator.visitor;
import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;

public class SatOpsVisitorImpl extends SatOpsBaseVisitor<String> {

    @Override
    public String visitProgram(SatOpsParser.ProgramContext ctx) {
        // Visit all statements and concatenate results
        StringBuilder output = new StringBuilder();
        for (SatOpsParser.StatementContext stmt : ctx.statement()) {
            output.append(visitStatement(stmt)).append("\n");
        }
        return output.toString().trim(); // remove trailing newline
    }

    @Override
    public String visitStatement(SatOpsParser.StatementContext ctx) {
        // Extract the string without quotes
        String raw = ctx.STRING().getText();
        return raw.substring(1, raw.length() - 1);
    }
}

