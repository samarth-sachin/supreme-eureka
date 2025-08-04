package com.dsl.simulator.visitor;

import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;

public class SatOpsVisitorImpl extends SatOpsBaseVisitor<String> {

    StringBuilder output = new StringBuilder();

    @Override
    public String visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        return output.toString().trim();
    }

    @Override
    public String visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satellite = ctx.deployStmt().ID().getText();
        output.append("Deployed ").append(satellite).append("\n");
        return null;
    }

    @Override
    public String visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satellite = ctx.moveStmt().ID().getText();
        String x = ctx.moveStmt().INT(0).getText();
        String y = ctx.moveStmt().INT(1).getText();
        output.append("Moved ").append(satellite).append(" to (").append(x).append(", ").append(y).append(")\n");
        return null;
    }

    @Override
    public String visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.printStmt().STRING().getText().replaceAll("^\"|\"$", "");
        output.append("Message: ").append(message).append("\n");
        return null;
    }
}
