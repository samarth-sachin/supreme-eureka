package com.dsl.simulator.visitor;

import com.dsl.simulator.Orekit.VisibilityUtil;
import com.dsl.simulator.Predictor.PassPredictor;
import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.Product.Orbit;
import com.dsl.simulator.Product.Satellite;
import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import lombok.Getter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.models.earth.atmosphere.data.CssiSpaceWeatherData;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Getter
public class SatOpsVisitor extends SatOpsBaseVisitor<Void> {

    @Getter
    private Orbit currentOrbit; // Store last simulated orbit (if you use it elsewhere)
    private double lastVelocity;

    // Track satellites and ground stations
    private final Map<String, Satellite> satellites = new HashMap<>();
    private final Map<String, GroundStation> groundStations = new HashMap<>();
    private final List<String> logs = new ArrayList<>();

    // Message queue with light-time delivery
    private static class QueuedMessage {
        final String satId;
        final String gsId;
        final String text;
        final Instant deliverAt;
        QueuedMessage(String satId, String gsId, String text, Instant deliverAt) {
            this.satId = satId; this.gsId = gsId; this.text = text; this.deliverAt = deliverAt;
        }
    }
    private final List<QueuedMessage> queue = new ArrayList<>();

    // ---------------- Program ----------------
    @Override
    public Void visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        return null;
    }

    // ---------------- deploy (physics) ----------------
    // Behavior:
    //  - Try to load a TLE from classpath: /tle/<satId>.tle (two lines)
    //  - If found: create a physics-based Satellite with TLEPropagator
    //  - Else: fall back to dummy (0,0)
    @Override
    public Void visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satId = ctx.ID().getText();

        Optional<TLE> tleOpt = loadTleFromClasspath(satId);
        if (tleOpt.isPresent()) {
            TLE tle = tleOpt.get();
            Propagator prop = TLEPropagator.selectExtrapolator(tle);
            AbsoluteDate epoch = VisibilityUtil.nowUtc(); // start “now”
            Satellite sat = new Satellite(satId, prop, epoch);
            satellites.put(satId, sat);
            String msg = "Deployed (physics): " + satId + " with TLE epoch " + tle.getDate();
            System.out.println(msg);
            logs.add(msg);
        } else {
            Satellite sat = new Satellite(satId, 0, 0);
            satellites.put(satId, sat);
            String msg = "Deployed (dummy XY): " + sat;
            System.out.println(msg);
            logs.add(msg);
            logs.add("Hint: place a TLE file at classpath /tle/" + satId + ".tle to enable physics mode.");
        }
        return null;
    }

    // ---------------- move ----------------
    // Behavior:
    //  - Physics mode: interpret first NUMBER as seconds to advance satellite time; second NUMBER ignored
    //  - Dummy mode: keep your old setPosition(x,y)
    @Override
    public Void visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satId = ctx.ID().getText();
        double a = Double.parseDouble(ctx.NUMBER(0).getText());
        double b = Double.parseDouble(ctx.NUMBER(1).getText());

        Satellite sat = satellites.get(satId);
        if (sat == null) {
            String m = "Error: " + satId + " not deployed yet.";
            System.out.println(m);
            logs.add(m);
            return null;
        }

        if (sat.isPhysicsBased()) {
            double dtSeconds = a; // interpret first number as Δt
            sat.advanceTime(dtSeconds);
            AbsoluteDate nowSat = sat.getCurrentDate();
            // Log propagated position for info
            try {
                SpacecraftState st = sat.getPropagator().propagate(nowSat);
                Vector3D posECI = st.getPVCoordinates().getPosition();
                String m = "Moved (physics): " + satId + " advanced by " + dtSeconds + "s. Epoch=" + nowSat
                        + " | r=" + String.format(Locale.US,"%.0f,%.0f,%.0f", posECI.getX(), posECI.getY(), posECI.getZ()) + " m";
                System.out.println(m);
                logs.add(m);
            } catch (Exception e) {
                String m = "Error propagating " + satId + ": " + e.getMessage();
                System.out.println(m);
                logs.add(m);
            }
        } else {
            int x = (int) Math.round(a);
            int y = (int) Math.round(b);
            sat.setpostion(x, y);
            String m = "Moved (dummy): " + sat;
            System.out.println(m);
            logs.add(m);
        }
        return null;
    }

    // ---------------- print ----------------
    @Override
    public Void visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        System.out.println(message);
        logs.add("Message: " + message);
        return null;
    }

    // ---------------- simulateOrbit (unchanged placeholder) ----------------
    @Override
    public Void visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double sma = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());

        try {
            // Your existing simulator (not provided here)
            String result = "sma=" + sma + " ecc=" + ecc + " inc=" + inc;
            System.out.println("Simulating orbit (placeholder): " + result);
            logs.add("Simulating orbit (placeholder): " + result);
        } catch (Exception e) {
            System.err.println("Error during orbit simulation: " + e.getMessage());
            logs.add("Error during orbit simulation: " + e.getMessage());
        }

        return null;
    }

    // ---------------- deployGroundStation ----------------
    @Override
    public Void visitDeployGroundStationStatement(SatOpsParser.DeployGroundStationStatementContext ctx) {
        String name = ctx.ID().getText();
        double lat = Double.parseDouble(ctx.NUMBER(0).getText());
        double lon = Double.parseDouble(ctx.NUMBER(1).getText());

        GroundStation gs = new GroundStation(name, lat, lon);
        groundStations.put(name, gs);

        String m = "Deployed Ground Station: " + gs;
        System.out.println(m);
        logs.add(m);
        return null;
    }

    // ---------------- link (physics visibility) ----------------
    @Override
    public Void visitLinkStatement(SatOpsParser.LinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId  = ctx.ID(1).getText();

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat == null || gs == null) {
            String m = "Error: Cannot link, objects not found.";
            System.out.println(m);
            logs.add(m);
            return null;
        }

        double minElev = (gs.minElevationDeg > 0 ? gs.minElevationDeg : 10.0);

        try {
            boolean visibleNow = false;

            if (sat.isPhysicsBased()) {
                AbsoluteDate when = (sat.getCurrentDate() != null) ? sat.getCurrentDate() : VisibilityUtil.nowUtc();
                visibleNow = VisibilityUtil.isVisibleAt(
                        sat.getPropagator(),
                        when,
                        gs.getLatitude(),
                        gs.getLongitude(),
                        minElev
                );
            } else {
                // No physics → cannot verify visibility → refuse physical link
                visibleNow = false;
            }

            if (visibleNow) {
                sat.linkedStations.add(gsId);
                gs.linkedSatellites.add(satId);
                String m = "Linked (visible) satellite " + satId + " to ground station " + gsId;
                System.out.println(m);
                logs.add(m);
            } else {
                String m = "Not visible now: " + satId + " -> " + gsId + " (below " + minElev + "°).";
                System.out.println(m);
                logs.add(m);

                if (sat.isPhysicsBased()) {
                    PassPredictor.nextPassWindow(
                            sat.getPropagator(),
                            gs.getLatitude(),
                            gs.getLongitude(),
                            minElev,
                            12 * 3600
                    ).ifPresentOrElse(
                            logs::add,
                            () -> logs.add("No pass predicted in next 12h for " + satId + " -> " + gsId)
                    );
                } else {
                    logs.add("Satellite " + satId + " is in dummy mode; deploy with TLE to enable visibility checks.");
                }
            }
        } catch (Exception e) {
            String m = "Error during visibility check: " + e.getMessage();
            System.out.println(m);
            logs.add(m);
        }

        return null;
    }

    // ---------------- unlink ----------------
    @Override
    public Void visitUnlinkStatement(SatOpsParser.UnlinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat != null && gs != null) {
            sat.linkedStations.remove(gsId);
            gs.linkedSatellites.remove(satId);
            String m = "Unlinked satellite " + satId + " from ground station " + gsId;
            System.out.println(m);
            logs.add(m);
        } else {
            String m = "Error: Cannot unlink, objects not found.";
            System.out.println(m);
            logs.add(m);
        }
        return null;
    }

    // ---------------- send (validate link + add delay) ----------------
    @Override
    public Void visitSendStatement(SatOpsParser.SendStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String message = ctx.STRING().getText().replace("\"", "");

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat == null || gs == null) {
            String m = "Error: Cannot send message (objects not found).";
            System.out.println(m);
            logs.add(m);
            return null;
        }

        if (!sat.linkedStations.contains(gsId)) {
            String m = "Error: Cannot send message — no active link between " + satId + " and " + gsId;
            System.out.println(m);
            logs.add(m);
            return null;
        }

        // Compute light-time delay if physics; else zero delay
        double delaySec = 0.0;
        if (sat.isPhysicsBased()) {
            try {
                delaySec = computeLightTimeSeconds(sat, gs);
            } catch (Exception e) {
                // if range fails, keep delay 0 but warn
                logs.add("Warning: could not compute light-time delay: " + e.getMessage());
            }
        }

        Instant deliverAt = Instant.now().plusMillis((long) (delaySec * 1000.0));
        queue.add(new QueuedMessage(satId, gsId, message, deliverAt));

        String m = String.format(Locale.US,
                "Queued message from %s to %s: \"%s\" (delay ~ %.3f s, ETA %s)",
                satId, gsId, message, delaySec, deliverAt.toString());
        System.out.println(m);
        logs.add(m);
        return null;
    }

    // ---------------- receive (deliver if arrived) ----------------
    @Override
    public Void visitReceiveStatement(SatOpsParser.ReceiveStatementContext ctx) {
        String gsId = ctx.ID(0).getText();
        String satId = ctx.ID(1).getText();

        GroundStation gs = groundStations.get(gsId);
        if (gs == null) {
            String m = "Error: Ground station " + gsId + " not found.";
            System.out.println(m);
            logs.add(m);
            return null;
        }

        Instant now = Instant.now();
        List<String> delivered = new ArrayList<>();
        Iterator<QueuedMessage> it = queue.iterator();
        while (it.hasNext()) {
            QueuedMessage qm = it.next();
            if (qm.gsId.equals(gsId) && qm.satId.equals(satId) && !now.isBefore(qm.deliverAt)) {
                delivered.add(qm.text);
                gs.messages.add("From " + satId + ": " + qm.text);
                it.remove();
            }
        }

        if (!delivered.isEmpty()) {
            String m = gsId + " received from " + satId + ": " + delivered;
            System.out.println(m);
            logs.add(m);
        } else {
            // Any in transit?
            long inTransit = queue.stream()
                    .filter(q -> q.gsId.equals(gsId) && q.satId.equals(satId) && now.isBefore(q.deliverAt))
                    .count();
            if (inTransit > 0) {
                Optional<QueuedMessage> next = queue.stream()
                        .filter(q -> q.gsId.equals(gsId) && q.satId.equals(satId))
                        .min(Comparator.comparing(q -> q.deliverAt));
                String eta = next.map(q -> Duration.between(now, q.deliverAt).toMillis() + " ms").orElse("unknown");
                String m = "No messages ready; " + inTransit + " in transit (next ETA ~ " + eta + ").";
                System.out.println(m);
                logs.add(m);
            } else {
                String m = "No messages found from " + satId + " at " + gsId;
                System.out.println(m);
                logs.add(m);
            }
        }
        return null;
    }

    // ---------------- predictPass ----------------
    @Override
    public Void visitPredictPassStatement(SatOpsParser.PredictPassStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId  = ctx.ID(1).getText();

        Satellite sat = satellites.get(satId);
        GroundStation gs = groundStations.get(gsId);

        if (sat == null) {
            System.err.println("Error: Satellite " + satId + " not found.");
            return null;
        }
        if (gs == null) {
            System.err.println("Error: GroundStation " + gsId + " not found.");
            return null;
        }
        if (!sat.isPhysicsBased()) {
            String m = "Satellite " + satId + " is in dummy mode; deploy with TLE to enable pass prediction.";
            System.out.println(m);
            logs.add(m);
            return null;
        }

        try {
            Optional<String> window = PassPredictor.nextPassWindow(
                    sat.getPropagator(),
                    gs.getLatitude(),
                    gs.getLongitude(),
                    (gs.minElevationDeg > 0 ? gs.minElevationDeg : 10.0),
                    86400
            );

            window.ifPresentOrElse(
                    w -> {
                        String m = "Next pass of " + satId + " over " + gsId + ": " + w;
                        System.out.println(m);
                        logs.add(m);
                    },
                    () -> {
                        String m = "No pass in the next 24h for " + satId + " over " + gsId;
                        System.out.println(m);
                        logs.add(m);
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ---------------- Helpers ----------------

    /** Try to load a TLE from classpath resource /tle/<satId>.tle (expects exactly 2 lines). */
    private Optional<TLE> loadTleFromClasspath(String satId) {
        String path = "/tle/" + satId + ".tle";
        try (InputStream is = SatOpsVisitor.class.getResourceAsStream(path)) {
            if (is == null) return Optional.empty();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String l1 = br.readLine();
                String l2 = br.readLine();
                if (l1 != null && l2 != null && l1.startsWith("1 ") && l2.startsWith("2 ")) {
                    return Optional.of(new TLE(l1.trim(), l2.trim()));
                }
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    /** Speed of light (m/s) */
    private static final double C = 299_792_458.0;

    /** Compute one-way light time (s) from satellite to ground station at satellite’s current epoch. */
    private double computeLightTimeSeconds(Satellite sat, GroundStation gs) {
        // Frame & Earth
        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, itrf);

        // Ground Cartesian in ITRF
        GeodeticPoint site = new GeodeticPoint(
                Math.toRadians(gs.getLatitude()),
                Math.toRadians(gs.getLongitude()),
                0.0
        );

// Get site position in ITRF
     Vector3D rSite = earth.transform(site);
//        Vector3D rSite = earth.transform(site).getPVCoordinates(null, inertialFrame).getPosition();









        // Satellite state at its current epoch (fallback to now if null)
        AbsoluteDate when = (sat.getCurrentDate() != null) ? sat.getCurrentDate() : VisibilityUtil.nowUtc();
        SpacecraftState s = sat.getPropagator().propagate(when);
        Vector3D rSatITRF = s.getPVCoordinates(itrf).getPosition();


        double range = rSatITRF.subtract(rSite).getNorm(); // meters
        return range / C;
    }

    // Debug print
    public void printAll() {
        System.out.println("=== Satellites ===");
        satellites.values().forEach(System.out::println);

        System.out.println("=== Ground Stations ===");
        groundStations.values().forEach(System.out::println);
    }
}
