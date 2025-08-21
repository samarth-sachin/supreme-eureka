package com.dsl.simulator.Predictor;

import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventsLogger;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class PassPredictor {

    public static Optional<String> nextPassWindow(
            Propagator propagator,
            double latDeg,
            double lonDeg,
            double minElevDeg,
            double searchSeconds) {

        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);

        OneAxisEllipsoid earth = new OneAxisEllipsoid(
                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                itrf
        );

        GeodeticPoint site = new GeodeticPoint(Math.toRadians(latDeg), Math.toRadians(lonDeg), 0.0);
        TopocentricFrame topo = new TopocentricFrame(earth, site, "gs");

        ElevationDetector det = new ElevationDetector(topo)
                .withConstantElevation(Math.toRadians(minElevDeg))
                .withHandler(new ContinueOnEvent<ElevationDetector>());

        EventsLogger logger = new EventsLogger();
        var monitored = logger.monitorDetector(det);

        AbsoluteDate start = new AbsoluteDate(Date.from(Instant.now()), TimeScalesFactory.getUTC());
        AbsoluteDate end   = start.shiftedBy(searchSeconds);

        try {
            propagator.clearEventsDetectors();
            propagator.addEventDetector(monitored);
            propagator.propagate(start, end);
        } catch (Exception ex) {
            return Optional.empty();
        }

        var events = logger.getLoggedEvents();
        if (events.size() < 2) return Optional.empty();

        AbsoluteDate aos = events.get(0).getState().getDate();
        AbsoluteDate los = events.get(1).getState().getDate();
        return Optional.of("Next pass: " + aos + " to " + los + " (UTC)");
    }
}
