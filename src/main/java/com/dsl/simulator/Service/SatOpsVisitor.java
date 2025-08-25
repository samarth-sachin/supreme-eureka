package com.dsl.simulator.Service;

import com.dsl.simulator.Orekit.SatellitePropagation;
import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.Product.Satellite;
import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.MissionControlService;

import java.util.ArrayList;
import java.util.List;

public class SatOpsVisitor extends SatOpsBaseVisitor<Void> {

    private final MissionControlService missionControlService;
    private final List<String> logs = new ArrayList<>();
   private SatellitePropagation satellitePropagation=new SatellitePropagation();



    public SatOpsVisitor(MissionControlService missionControlService) {
        this.missionControlService = missionControlService;
    }

    public List<String> getLogs() {
        return logs;
    }

    @Override
    public Void visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        return null;
    }

    @Override
    public Void visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satId = ctx.ID().getText();
        Satellite sat = missionControlService.deploySatellite(satId);
        if (sat.isPhysicsBased()) {
            logs.add("Deployed (physics): " + sat.getSatelliteName());
            System.out.println("Deployed (physics): " + sat.getSatelliteName());
        } else {
            logs.add("Deployed (dummy): " + sat.getSatelliteName());
            System.out.println("Deployed (dummy): " + sat.getSatelliteName());
            logs.add("Hint: place a TLE file at classpath /tle/" + satId + ".tle to enable physics mode.");
            System.out.println("Hint: enable physics mode");
        }
        return null;
    }

    @Override
    public Void visitDeployGroundStationStatement(SatOpsParser.DeployGroundStationStatementContext ctx) {
        String gsId = ctx.ID().getText();
        double lat = Double.parseDouble(ctx.NUMBER(0).getText());
        double lon = Double.parseDouble(ctx.NUMBER(1).getText());
        GroundStation gs = missionControlService.deployGroundStation(gsId, lat, lon);
        logs.add("Deployed Ground Station: " + gs.getName());
        System.out.println("Deployed Ground Station: " + gs.getName());
        return null;
    }

    @Override
    public Void visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satId = ctx.ID().getText();
        double val1 = Double.parseDouble(ctx.NUMBER(0).getText());
        double val2 = Double.parseDouble(ctx.NUMBER(1).getText());
        try {
            missionControlService.moveSatellite(satId, val1, val2);
            logs.add("Moved: " + satId);
            System.out.println("Moved: " + satId);
        } catch (IllegalArgumentException e) {
            logs.add("Error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Void visitLinkStatement(SatOpsParser.LinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        logs.add(missionControlService.link(satId, gsId));
        return null;
    }

    @Override
    public Void visitUnlinkStatement(SatOpsParser.UnlinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        logs.add(missionControlService.unlink(satId, gsId));
        return null;
    }

    @Override
    public Void visitSendStatement(SatOpsParser.SendStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        logs.add(missionControlService.sendMessage(satId, gsId, message));
        return null;
    }

    @Override
    public Void visitReceiveStatement(SatOpsParser.ReceiveStatementContext ctx) {
        String gsId = ctx.ID(0).getText();
        String satId = ctx.ID(1).getText();
        List<String> received = missionControlService.receiveMessages(gsId, satId);
        if (received.isEmpty()) {
            logs.add("No messages received at " + gsId + " from " + satId);
            System.out.println("No messages received at " + gsId + " from " + satId);
        } else {
            logs.addAll(received);
        }
        return null;
    }

    @Override
    public Void visitPredictPassStatement(SatOpsParser.PredictPassStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String result = missionControlService.predictPass(satId, gsId)
                .orElse("No pass predicted in the next 24h for " + satId + " over " + gsId);

        logs.add(result);
        return null;
    }

    @Override
    public Void visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        logs.add("Message: " + message);
        System.out.println("Message: " + message);
        return null;
    }

    @Override
    public Void visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());
//        logs.add(satellitePropagation.simulateOrbit(sma, ecc, inc));
        String result = satellitePropagation.simulateOrbit(sma * 1000, ecc, inc);

        // 3. Add the results to the log
        logs.add("--- Orbit Simulation Results ---");
        System.out.println(result);
        logs.add(result);
        logs.add("------------------------------");

        return null;
    }
    @Override
    public Void visitManeuverStatement(SatOpsParser.ManeuverStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        double deltaV = Double.parseDouble(ctx.NUMBER().getText());
        String direction=ctx.ID(1).getText();
        String result=missionControlService.executeManeuver(satId, deltaV, direction);
        logs.add(result);
        return null;
    }
    // In your SatOpsVisitor.java class



}