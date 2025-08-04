package com.dsl.simulator.visitor;

import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;

public class SatOpsVisitorImpl extends SatOpsBaseVisitor<String> {
    @Override
    public String visitProgram(SatOpsParser.ProgramContext ctx) {
        StringBuilder result = new StringBuilder();
        for (SatOpsParser.StatementContext stmt : ctx.statement()) {
            result.append(visit(stmt)).append("\n");
        }
        return result.toString();
    }

    @Override
    public String visitStatement(SatOpsParser.StatementContext ctx) {
        if (ctx.deploySatellite() != null)
            return "Deploying satellite: " + ctx.STRING().getText();
        else if (ctx.moveSatellite() != null)
            return "Moving satellite " + ctx.STRING().getText() +
                    " to coordinates " + ctx.coordinates().getText();
        else if (ctx.print() != null)
            return ctx.STRING().getText().replace("\"", "");
        return "Unknown command";
    }
}
