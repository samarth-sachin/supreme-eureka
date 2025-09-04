package com.dsl.simulator.Product;

/**
 * Minimal Keplerian-ish orbit holder with a simple propagate().
 * Units: a in km, angles in degrees, time in seconds. (Lightweight for DSL demo)
 */
public class Orbit {
//   a=  Defines the size of the orbit (semi-major axis).
//    e = Defines the shape of the orbit (eccentricity, 0=circle).
//i = Defines the tilt of the orbit relative to the Equator (inclination).
//pa = Defines the orientation of the orbit within its plane (argument of perigee).
//raan = Defines the swivel of the orbit's plane around the Earth (RAAN).
//v = Defines the satellite's current position along its orbit (true anomaly).
    private double a;
    private double e;
    private double i;
    private double pa;
    private double raan;// RAAN (deg)
    private double v;   // true anomaly (deg)

    private static final double MU = 398600.4418; // earth's GM (km^3/s^2)

    public Orbit(double aKm, double e, double iDeg) {
        this.a = aKm;
        this.e = e;
        this.i = iDeg;
        this.pa = 0.0;
        this.raan = 0.0;
        this.v = 0.0;
    }

    /** Super-simplified propagation assuming near-circular: v ~ n*t */
    public void propagate(double dtSeconds) {
        double n = Math.sqrt(MU / Math.pow(a, 3)); // rad/s
        double M = n * dtSeconds;                  // rad
        this.v = Math.toDegrees(M % (2 * Math.PI));
    }

    @Override
    public String toString() {
        return "Keplerian parameters: {a: " + a + "; e: " + e + "; i: " + i +
                "; pa: " + pa + "; raan: " + raan + "; v: " + v + ";}";
    }
}
