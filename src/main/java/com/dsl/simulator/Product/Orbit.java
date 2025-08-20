package com.dsl.simulator.Product;

public class Orbit {
    private double sma;   // semi-major axis (km)
    private double ecc;   // eccentricity
    private double inc;   // inclination (deg)
    private double v;     // true anomaly (deg)

    private static final double MU = 398600.4418; // Earth's GM (km^3/s^2)

    public Orbit(double sma, double ecc, double inc) {
        this.sma = sma;
        this.ecc = ecc;
        this.inc = inc;
        this.v = 0.0;
    }

    public void propagate(double dtSeconds) {
        // Mean motion (rad/s)
        double n = Math.sqrt(MU / Math.pow(sma, 3));

        // Mean anomaly (rad)
        double M = n * dtSeconds;

        // For near-circular orbits, approximate M ~ true anomaly
        this.v = Math.toDegrees(M % (2 * Math.PI));
    }

    @Override
    public String toString() {
        return "Keplerian parameters: {a: " + sma +
                "; e: " + ecc +
                "; i: " + inc +
                "; v: " + v + "}";
    }
}
