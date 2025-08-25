package com.dsl.simulator.Service;

import com.dsl.simulator.Orekit.VisibilityUtil;
import com.dsl.simulator.Predictor.PassPredictor;
import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.Product.Satellite;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.forces.maneuvers.ImpulseManeuver;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;
import org.springframework.stereotype.Service;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.events.DateDetector;
import org.orekit.time.TimeScalesFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissionControlService {

    // The application's state (satellites and ground stations) now lives in the service.
    private final Map<String, Satellite> activeSatellites = new HashMap<>();
    private final Map<String, GroundStation> activeGroundStations = new HashMap<>();

    // The message queue also lives in the service now
    private static class QueuedMessage {
        final String satId, gsId, text;
        final Instant deliverAt;
        QueuedMessage(String satId, String gsId, String text, Instant deliverAt) {
            this.satId = satId; this.gsId = gsId; this.text = text; this.deliverAt = deliverAt;
        }
    }
    private final List<QueuedMessage> queue = new ArrayList<>();


    public Satellite deploySatellite(String satId) {
        Optional<TLE> tleOpt = loadTleFromClasspath(satId);
        Satellite sat;
        if (tleOpt.isPresent()) {
            TLE tle = tleOpt.get();
            Propagator prop = TLEPropagator.selectExtrapolator(tle);
            AbsoluteDate epoch = VisibilityUtil.nowUtc();
            sat = new Satellite(satId, prop, epoch);
        } else {
            sat = new Satellite(satId, 0, 0);
        }
        activeSatellites.put(satId, sat);
        return sat;
    }

    public GroundStation deployGroundStation(String gsId, double lat, double lon) {
        GroundStation gs = new GroundStation(gsId, lat, lon);
        activeGroundStations.put(gsId, gs);
        return gs;
    }

    public Satellite moveSatellite(String satId, double val1, double val2) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null) throw new IllegalArgumentException("Satellite not found: " + satId);
        if (sat.isPhysicsBased()) {
            sat.advanceTime(val1);
        } else {
            sat.setPosition((int) val1, (int) val2);
        }
        return sat;
    }

    public String link(String satId, String gsId) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null) return "Error: Cannot link, objects not found.";
        if (!sat.isPhysicsBased()) return "Link failed: Satellite " + satId + " is in dummy mode.";

        boolean isVisible = VisibilityUtil.isVisibleAt(
                sat.getPropagator(), sat.getCurrentDate(), gs.getLatitude(), gs.getLongitude(), gs.minElevationDeg);

        if (isVisible) {
            sat.linkedStations.add(gsId);
            gs.linkedSatellites.add(satId);
            return "Linked (visible): " + satId + " <-> " + gsId;
        } else {
            return "Not visible now: " + satId + " -> " + gsId;
        }
    }

    public String unlink(String satId, String gsId) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null) return "Error: Cannot unlink, objects not found.";

        sat.linkedStations.remove(gsId);
        gs.linkedSatellites.remove(satId);
        return "Unlinked " + satId + " from " + gsId;
    }

    public String sendMessage(String satId, String gsId, String message) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null) return "Error: Cannot send message (objects not found).";
        if (!sat.linkedStations.contains(gsId)) return "Error: Cannot send message — no active link.";

        // For now, we use zero delay. This logic can be enhanced later.
        Instant deliverAt = Instant.now();
        queue.add(new QueuedMessage(satId, gsId, message, deliverAt));
        return String.format("Queued message from %s to %s: \"%s\"", satId, gsId, message);
    }

    public List<String> receiveMessages(String gsId, String satId) {
        GroundStation gs = activeGroundStations.get(gsId);
        if (gs == null) return List.of("Error: Ground station " + gsId + " not found.");

        Instant now = Instant.now();
        List<String> deliveredMessages = new ArrayList<>();
        Iterator<QueuedMessage> it = queue.iterator();
        while(it.hasNext()) {
            QueuedMessage qm = it.next();
            if(qm.gsId.equals(gsId) && qm.satId.equals(satId) && !now.isBefore(qm.deliverAt)) {
                deliveredMessages.add("From " + satId + ": " + qm.text);
                it.remove();
            }
        }
        return deliveredMessages;
    }

    public Optional<String> predictPass(String satId, String gsId) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null || !sat.isPhysicsBased()) {
            return Optional.of("Cannot predict pass: Satellite or GS not found or not in physics mode.");
        }
        return PassPredictor.nextPassWindow(
                sat.getPropagator(), gs.getLatitude(), gs.getLongitude(), gs.minElevationDeg, 86400);
    }

    public String simulateOrbit(double sma, double ecc, double inc) {
        // Placeholder for your orbit simulation logic
        return "Simulating orbit with sma=" + sma + ", ecc=" + ecc + ", inc=" + inc;
    }

    // Helper method to load TLE files
    private Optional<TLE> loadTleFromClasspath(String satId) {
        String path = "/tle/" + satId + ".tle";
        try (InputStream is = getClass().getResourceAsStream(path)) {
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
    public String executeManeuver(String satId, double deltaV_kms, String direction) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null || !sat.isPhysicsBased()) {
            return "Error: Satellite not found or not in physics mode.";
        }

        try {
            Orbit initialOrbit = sat.getPropagator().getInitialState().getOrbit();
            AbsoluteDate maneuverDate = sat.getCurrentDate();

            Vector3D burnDirection;
            if ("prograde".equalsIgnoreCase(direction)) {
                PVCoordinates pv = initialOrbit.getPVCoordinates(FramesFactory.getEME2000());
                burnDirection = pv.getVelocity().normalize();
            } else {
                return "Error: Unknown maneuver direction '" + direction + "'";
            }

            DateDetector dateDetector = new DateDetector(maneuverDate);
            ImpulseManeuver<DateDetector> maneuverHandler = new ImpulseManeuver<>(
                    dateDetector,
                    burnDirection,
                    deltaV_kms * 1000.0
            );
            dateDetector = dateDetector.withHandler(new ContinueOnEvent<>());

            KeplerianPropagator newPropagator = new KeplerianPropagator(initialOrbit);
            newPropagator.addEventDetector(dateDetector);

            SpacecraftState finalState = newPropagator.propagate(maneuverDate.shiftedBy(1.0));
            sat.setPropagator(new KeplerianPropagator(finalState.getOrbit()));

            return String.format("Maneuver successful for %s. New orbit calculated.", satId);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during maneuver calculation: " + e.getMessage();
        }
    }

}