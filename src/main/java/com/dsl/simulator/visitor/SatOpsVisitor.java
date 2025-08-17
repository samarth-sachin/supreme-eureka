package com.dsl.simulator.visitor;

import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Executor.CommandExecutor;

public class SatOpsVisitor extends SatOpsBaseVisitor<String> {

    private final StringBuilder output = new StringBuilder();
    private final CommandExecutor executor = new CommandExecutor();

    @Override
    public String visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        System.out.println(output.toString().trim()); // for server logs
        return output.toString().trim();
    }

    @Override
    public String visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satellite = ctx.ID().getText();
        output.append(executor.executeDeploy(satellite)).append("\n");
        return null;
    }

    @Override
    public String visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satellite = ctx.ID().getText();
        int x = Integer.parseInt(ctx.INT().get(0).getText());
        int y = Integer.parseInt(ctx.INT().get(1).getText());

        output.append(executor.executeMove(satellite, x, y)).append("\n");
        return null;
    }

    @Override
    public String visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        output.append(executor.executePrint(message)).append("\n");
        return null;
    }

    @Override
    public String visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());

        output.append(executor.executeSimulateOrbit(sma, ecc, inc)).append("\n");
        return null;
    }
}
