package com.dsl.simulator.visitor;

import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Product.Satellite;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
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

    // Add Ground Station Support
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

    @Override
    public Void visitLinkStatement(SatOpsParser.LinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat != null && gs != null) {
            sat.linkedStations.add(gsId);
            gs.linkedSatellites.add(satId);
            System.out.println("Linked satellite " + satId + " to ground station " + gsId);
        } else {
            System.out.println("Error: Cannot link, objects not found.");
        }
        return null;
    }

    @Override
    public Void visitUnlinkStatement(SatOpsParser.UnlinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat != null && gs != null) {
            sat.linkedStations.remove(gsId);
            gs.linkedSatellites.remove(satId);
            System.out.println("Unlinked satellite " + satId + " from ground station " + gsId);
        } else {
            System.out.println("Error: Cannot unlink, objects not found.");
        }
        return null;
    }

    @Override
    public Void visitSendStatement(SatOpsParser.SendStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String message = ctx.STRING().getText().replace("\"", "");

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat != null && gs != null && sat.linkedStations.contains(gsId)) {
            gs.messages.add("From " + satId + ": " + message);
            System.out.println("Message sent from " + satId + " to " + gsId + ": " + message);
        } else {
            System.out.println("Error: Cannot send message (link missing or objects not found).");
        }
        return null;
    }

    @Override
    public Void visitReceiveStatement(SatOpsParser.ReceiveStatementContext ctx) {
        String gsId = ctx.ID(0).getText();
        String satId = ctx.ID(1).getText();

        GroundStation gs = groundStations.get(gsId);

        if (gs != null) {
            List<String> msgs = gs.messages.stream()
                    .filter(m -> m.startsWith("From " + satId))
                    .toList();
            if (!msgs.isEmpty()) {
                System.out.println(gsId + " received from " + satId + ": " + msgs);
            } else {
                System.out.println("No messages found from " + satId + " at " + gsId);
            }
        } else {
            System.out.println("Error: Ground station " + gsId + " not found.");
        }
        return null;
    }
}
//package com.dsl.simulator.visitor;
//
//import com.dsl.simulator.Product.GroundStation;
//import com.dsl.simulator.SatOpsBaseVisitor;
//import com.dsl.simulator.SatOpsParser;
//import com.dsl.simulator.Product.Satellite;
//import lombok.Getter;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Getter
//public class SatOpsVisitor extends SatOpsBaseVisitor<Void> {
//
//    // Map to keep track of satellites
//    private Map<String, Satellite> satellites = new HashMap<>();
//    private Map<String, GroundStation> groundStations = new HashMap<>();
//    @Override
//    public Void visitProgram(SatOpsParser.ProgramContext ctx) {
//        ctx.statement().forEach(this::visit);
//        return null;
//    }
//
//    @Override
//    public Void visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
//        String satellite = ctx.ID().getText();
//
//        Satellite sat = new Satellite();
//        sat.setSatelliteName(satellite);
//        sat.setX(0);
//        sat.setY(0);
//
//        satellites.put(satellite, sat);
//        System.out.println("Deployed: " + sat);
//        return null;
//    }
//
//    @Override
//    public Void visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
//        String satellite = ctx.ID().getText();
//        int x = Integer.parseInt(ctx.NUMBER(0).getText());
//        int y = Integer.parseInt(ctx.NUMBER(1).getText());
//
//        Satellite sat = satellites.get(satellite);
//        if (sat != null) {
//            sat.setpostion(x, y);
//            System.out.println("Moved: " + sat);
//        } else {
//            System.out.println("Error: " + satellite + " not deployed yet.");
//        }
//        return null;
//    }
//
//    @Override
//    public Void visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
//        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
//        System.out.println(message);
//        return null;
//    }
//
//    @Override
//    public Void visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
//        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
//        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
//        double inc = Double.parseDouble(ctx.NUMBER(2).getText());
//
//        System.out.println("Simulating orbit -> SMA: " + sma + ", ECC: " + ecc + ", INC: " + inc);
//        return null;
//    }
//    //  Add Ground Station Support
//    @Override
//    public Void visitDeployGroundStationStatement(SatOpsParser.DeployGroundStationStatementContext ctx) {
//        String name = ctx.ID().getText();
//        double lat = Double.parseDouble(ctx.NUMBER(0).getText());
//        double lon = Double.parseDouble(ctx.NUMBER(1).getText());
//
//        GroundStation gs = new GroundStation(name, lat, lon);
//        groundStations.put(name, gs);
//
//        System.out.println("Deployed Ground Station: " + gs);
//        return null;
//    }
//
//    // Helper to print everything
//    public void printAll() {
//        System.out.println("=== Satellites ===");
//        satellites.values().forEach(System.out::println);
//
//        System.out.println("=== Ground Stations ===");
//        groundStations.values().forEach(System.out::println);
//    }
//    @Override
//    public String visitLinkStatement(SatOpsParser.LinkStatementContext ctx) {
//        String satId = ctx.ID(0).getText();
//        String gsId = ctx.ID(1).getText();
//
//        Satellite sat = satellites.get(satId);
//        GroundStation gs = groundStations.get(gsId);
//
//        if (sat != null && gs != null) {
//            sat.linkedStations.add(gsId);
//            gs.linkedSatellites.add(satId);
//            System.out.println("Linked satellite " + satId + " to ground station " + gsId);
//            return "Linked satellite " + satId + " to ground station " + gsId;
//        }
//        return "Error: Cannot link, objects not found.";
//    }
//
//    @Override
//    public Void visitUnlinkStatement(SatOpsParser.UnlinkStatementContext ctx) {
//        String satId = ctx.ID(0).getText();
//        String gsId = ctx.ID(1).getText();
//
//        Satellite sat = satellites.get(satId);
//        GroundStation gs = groundStations.get(gsId);
//
//        if (sat != null && gs != null) {
//            sat.linkedStations.remove(gsId);
//            gs.linkedSatellites.remove(satId);
//            System.out.println("Unlinked satellite " + satId + " from ground station " + gsId);
////            return "Unlinked satellite " + satId + " from ground station " + gsId;
//        }
//        return "Error: Cannot unlink, objects not found.";
//    }
//
//    @Override
//    public Void visitSendStatement(SatOpsParser.SendStatementContext ctx) {
//        String satId = ctx.ID(0).getText();
//        String gsId = ctx.ID(1).getText();
//        String message = ctx.STRING().getText().replace("\"", "");
//
//        Satellite sat = satellites.get(satId);
//        GroundStation gs = groundStations.get(gsId);
//
//        if (sat != null && gs != null && sat.linkedStations.contains(gsId)) {
//            gs.messages.add("From " + satId + ": " + message);
//            System.out.println("Message sent from " + satId + " to " + gsId + ": " + message);
////            System.out.println("Message sent from " + satId + " to " + gsId);
//        }
//        System.out.println("Error: Cannot send message (link missing or objects not found).");
//
//    }
//
//    @Override
//    public Void visitReceiveStatement(SatOpsParser.ReceiveStatementContext ctx) {
//        String gsId = ctx.ID(0).getText();
//        String satId = ctx.ID(1).getText();
//
//        GroundStation gs = groundStations.get(gsId);
//
//        if (gs != null) {
//            List<String> msgs = gs.messages.stream()
//                    .filter(m -> m.startsWith("From " + satId))
//                    .toList();
//            if (!msgs.isEmpty()) {
//                System.out.println(gsId + " received from " + satId + ": " + msgs);
////                System.out.println(gsId + " received from " + satId + ": " + msgs);
//            }
//        }
//        System.out.println("No messages found from " + satId + " at " + gsId);
//
//    }
//
//}
