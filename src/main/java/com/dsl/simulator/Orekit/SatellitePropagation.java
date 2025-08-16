package com.dsl.simulator.Orekit;

import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

public class SatellitePropagation {

    /** Propagate a simple Keplerian orbit and print states. */
    public void simulateOrbit(double smaMeters, double ecc, double incDegrees) {
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
                    PositionAngle.MEAN,        // <-- the correct enum
                    inertial,
                    t0,
                    Constants.EIGEN5C_EARTH_MU
            );

            KeplerianPropagator prop = new KeplerianPropagator(orbit0);

            // propagate 10 minutes
            SpacecraftState s10 = prop.propagate(t0.shiftedBy(600.0));
            // propagate 1 hour
            SpacecraftState s60 = prop.propagate(t0.shiftedBy(3600.0));

            System.out.println("Initial orbit: " + orbit0);
            System.out.println("State @ +10 min: " + s10.getOrbit());
            System.out.println("State @ +60 min: " + s60.getOrbit());

        } catch (Exception e) {
            throw new RuntimeException("Propagation failed: " + e.getMessage(), e);
        }
    }
}
