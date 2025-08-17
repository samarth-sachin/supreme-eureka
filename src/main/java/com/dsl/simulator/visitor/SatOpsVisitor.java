package com.dsl.simulator.visitor;

import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Product.Satellite;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SatOpsVisitor extends SatOpsBaseVisitor<Void> {

    // Map to keep track of satellites
    private Map<String, Satellite> satellites = new HashMap<>();
    private Map<String, GroundStation> groundStations = new HashMap<>();
    @Override
    public Void visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        return null;
    }

    @Override
    public Void visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satellite = ctx.ID().getText();

        Satellite sat = new Satellite();
        sat.setSatelliteName(satellite);
        sat.setX(0);
        sat.setY(0);

        satellites.put(satellite, sat);
        System.out.println("Deployed: " + sat);
        return null;
    }

    @Override
    public Void visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satellite = ctx.ID().getText();
        int x = Integer.parseInt(ctx.NUMBER(0).getText());
        int y = Integer.parseInt(ctx.NUMBER(1).getText());

        Satellite sat = satellites.get(satellite);
        if (sat != null) {
            sat.setpostion(x, y);
            System.out.println("Moved: " + sat);
        } else {
            System.out.println("Error: " + satellite + " not deployed yet.");
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        System.out.println(message);
        return null;
    }

    @Override
    public Void visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());

        System.out.println("Simulating orbit -> SMA: " + sma + ", ECC: " + ecc + ", INC: " + inc);
        return null;
    }
    //  Add Ground Station Support
    @Override
    public Void visitDeployGroundStationStatement(SatOpsParser.DeployGroundStationStatementContext ctx) {
        String name = ctx.ID().getText();
        double lat = Double.parseDouble(ctx.NUMBER(0).getText());
        double lon = Double.parseDouble(ctx.NUMBER(1).getText());

        GroundStation gs = new GroundStation(name, lat, lon);
        groundStations.put(name, gs);

        System.out.println("Deployed Ground Station: " + gs);
        return null;
    }

    // Helper to print everything
    public void printAll() {
        System.out.println("=== Satellites ===");
        satellites.values().forEach(System.out::println);

        System.out.println("=== Ground Stations ===");
        groundStations.values().forEach(System.out::println);
    }



}
