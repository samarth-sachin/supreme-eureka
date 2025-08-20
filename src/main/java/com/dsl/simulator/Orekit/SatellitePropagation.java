package com.dsl.simulator.Orekit;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import java.util.ArrayList;
import java.util.List;

public class SatellitePropagation {
    private final List<String> lo = new ArrayList<>();

    /** Propagate a simple Keplerian orbit and print states. */
    public String simulateOrbit(double smaMeters, double ecc, double incDegrees) {
        try {
            var inertial = FramesFactory.getEME2000();
            AbsoluteDate t0 = new AbsoluteDate(2025, 1, 1, 12, 0, 0.0, TimeScalesFactory.getUTC());

            Orbit orbit0 = new KeplerianOrbit(
                    smaMeters,                 // semi-major axis (m)
                    ecc,                       // eccentricity
                    Math.toRadians(incDegrees),// inclination (rad)
                    0.0,                       // argument of perigee (rad)
                    0.0,                       // RAAN (rad)
                    0.0,                       // mean anomaly (rad)
                    PositionAngle.MEAN,
                    inertial,
                    t0,
                    Constants.EIGEN5C_EARTH_MU
            );

            KeplerianPropagator propagator = new KeplerianPropagator(orbit0);

            // Initial state
            PVCoordinates pvInit = orbit0.getPVCoordinates();
            Vector3D posInit = pvInit.getPosition();
            Vector3D velInit = pvInit.getVelocity();

            lo.add(String.format("Initial Position (km): (%.2f, %.2f, %.2f)",
                    posInit.getX()/1000, posInit.getY()/1000, posInit.getZ()/1000));
            lo.add(String.format("Initial Velocity (km/s): (%.3f, %.3f, %.3f)",
                    velInit.getX()/1000, velInit.getY()/1000, velInit.getZ()/1000));

            //  10 min
            SpacecraftState state10 = propagator.propagate(t0.shiftedBy(600));
            PVCoordinates pv10 = state10.getPVCoordinates();
            lo.add(String.format("T+10min Pos (km): (%.2f, %.2f, %.2f)",
                    pv10.getPosition().getX()/1000, pv10.getPosition().getY()/1000, pv10.getPosition().getZ()/1000));
            lo.add(String.format("T+10min Vel (km/s): (%.3f, %.3f, %.3f)",
                    pv10.getVelocity().getX()/1000, pv10.getVelocity().getY()/1000, pv10.getVelocity().getZ()/1000));

            //  60 min
            SpacecraftState state60 = propagator.propagate(t0.shiftedBy(3600));
            PVCoordinates pv60 = state60.getPVCoordinates();
            lo.add(String.format("T+60min Pos (km): (%.2f, %.2f, %.2f)",
                    pv60.getPosition().getX()/1000, pv60.getPosition().getY()/1000, pv60.getPosition().getZ()/1000));
            lo.add(String.format("T+60min Vel (km/s): (%.3f, %.3f, %.3f)",
                    pv60.getVelocity().getX()/1000, pv60.getVelocity().getY()/1000, pv60.getVelocity().getZ()/1000));

            return String.join("\n", lo);

        } catch (Exception e) {
            throw new RuntimeException("Propagation failed: " + e.getMessage(), e);
        }
    }
}
