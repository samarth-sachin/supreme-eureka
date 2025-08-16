package com.dsl.simulator.visitor;// package com.dsl.simulator.visitor;

import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

public class SatOpsVisitorImpl extends SatOpsBaseVisitor<String> {

    StringBuilder output = new StringBuilder();

    @Override
    public String visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        System.out.println(output.toString().trim());
        return output.toString().trim();
    }

    @Override
    public String visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satellite = ctx.ID().getText();
        output.append("Deployed ").append(satellite).append("\n");

        // Example: Orekit Earth model usage
        OneAxisEllipsoid earth = new OneAxisEllipsoid(
                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                FramesFactory.getITRF(IERSConventions.IERS_2010, true)
        );

        output.append("Earth model loaded for ").append(satellite).append("\n");
        return null;
    }

    @Override
    public String visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satellite = ctx.ID().getText();
        String x = ctx.INT().get(0).getText();
        String y = ctx.INT().get(1).getText();

        output.append("Moved ").append(satellite)
                .append(" to (").append(x).append(", ").append(y).append(")\n");
        return null;
    }

    @Override
    public String visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        output.append("Message: ").append(message).append("\n");
        return null;
    }
    @Override
    public String visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());

        output.append("Simulating orbit with parameters: ")
                .append("SMA=").append(sma)
                .append(", ECC=").append(ecc)
                .append(", INC=").append(inc).append("\n");

        // call your executor to run Orekit simulation
        new com.dsl.simulator.Executor.SatOpsExecutor().executeSimulateOrbit(sma, ecc, inc);

        return null;
    }

}

//package com.dsl.simulator.visitor;
//
//import com.dsl.simulator.SatOpsBaseVisitor;
//import com.dsl.simulator.SatOpsParser;
//import org.orekit.bodies.OneAxisEllipsoid;
//import org.orekit.frames.FramesFactory;
//import org.orekit.utils.Constants;
//import org.orekit.utils.IERSConventions;
//
//public class SatOpsVisitorImpl extends SatOpsBaseVisitor<String> {
//
//    StringBuilder output = new StringBuilder();
//
//    @Override
//    public String visitProgram(SatOpsParser.ProgramContext ctx) {
//        ctx.statement().forEach(this::visit);
//        System.out.println(output.toString().trim());
//        return output.toString().trim();
//    }
//
//    @Override
//    public String visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
//        // get ID from deployStmt rule
//        String satellite = ctx.deployStmt().ID().getText();
//        output.append("Deployed ").append(satellite).append("\n");
//
//        // Example: Orekit Earth model usage
//        OneAxisEllipsoid earth = new OneAxisEllipsoid(
//                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
//                Constants.WGS84_EARTH_FLATTENING,
//                FramesFactory.getITRF(IERSConventions.IERS_2010, true)
//        );
//
//        output.append("Earth model loaded for ").append(satellite).append("\n");
//        return null;
//    }
//
//    @Override
//    public String visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
//        String satellite = ctx.moveStmt().ID().getText();
//        String x = ctx.moveStmt().INT(0).getText();
//        String y = ctx.moveStmt().INT(1).getText();
//        output.append("Moved ").append(satellite)
//                .append(" to (").append(x).append(", ").append(y).append(")\n");
//        return null;
//    }
//
//    @Override
//    public String visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
//        String message = ctx.printStmt().STRING().getText()
//                .replaceAll("^\"|\"$", "");
//        output.append("Message: ").append(message).append("\n");
//        return null;
//    }
//}
