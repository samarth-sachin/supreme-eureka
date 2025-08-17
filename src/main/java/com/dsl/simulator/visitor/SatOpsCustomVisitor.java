package com.dsl.simulator.visitor;

import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.controller.CommandExecutor;

public class SatOpsCustomVisitor extends SatOpsBaseVisitor<Void> {

    private final CommandExecutor executor = new CommandExecutor();

    @Override
    public Void visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String id = ctx.ID().getText();
        executor.deploySatellite(id);
        return null;
    }

    @Override
    public Void visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String id = ctx.ID().getText();
        int x = Integer.parseInt(ctx.NUMBER(0).getText());
        int y = Integer.parseInt(ctx.NUMBER(1).getText());
        executor.moveSatellite(id, x, y);
        return null;
    }

    @Override
    public Void visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String msg = ctx.STRING().getText();
        executor.printMessage(msg.substring(1, msg.length() - 1)); // remove quotes
        return null;
    }

    @Override
    public Void visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());
        executor.simulateOrbit(sma, ecc, inc);
        return null;
    }
}
