package com.dsl.simulator.Orekit;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.time.Instant;
import java.util.Date;

public class VisibilityUtil {

    public static AbsoluteDate nowUtc() {
        return new AbsoluteDate(Date.from(Instant.now()), TimeScalesFactory.getUTC());
    }
    public static OneAxisEllipsoid wgs84Earth(Frame bodyFrame) {
        return new OneAxisEllipsoid(
                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                bodyFrame
        );
    }
    public static double elevationDeg(SpacecraftState state, double latDeg, double lonDeg, double altMeters, AbsoluteDate date) {
        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        OneAxisEllipsoid earth = wgs84Earth(itrf);
        GeodeticPoint site = new GeodeticPoint(Math.toRadians(latDeg), Math.toRadians(lonDeg), altMeters);
        TopocentricFrame topo = new TopocentricFrame(earth, site, "gs");
        Vector3D posITRF = state.getPVCoordinates(itrf).getPosition();
        double elevRad = topo.getElevation(posITRF, itrf, date);
        return Math.toDegrees(elevRad);
    }

    // is visible now for TLE-based propagator
    public static boolean isVisibleNow(TLEPropagator prop, double latDeg, double lonDeg, double minElevDeg) {
        AbsoluteDate now = nowUtc();
        SpacecraftState s = prop.propagate(now);
        double el = elevationDeg(s, latDeg, lonDeg, 0.0, now);
        return el >= minElevDeg;
    }
// is visible from now using keplerian orbit
    public static boolean isVisibleNow(KeplerianPropagator prop, double latDeg, double lonDeg, double minElevDeg) {
        AbsoluteDate now = nowUtc();
        SpacecraftState s = prop.propagate(now);
        double el = elevationDeg(s, latDeg, lonDeg, 0.0, now);
        return el >= minElevDeg;
    }
//is this visible from this or not
    public static boolean isVisibleAt(Propagator prop, AbsoluteDate when, double latDeg, double lonDeg, double minElevDeg) {
        SpacecraftState s = prop.propagate(when);
        double el = elevationDeg(s, latDeg, lonDeg, 0.0, when);
        return el >= minElevDeg;
    }
}
