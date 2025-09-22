package com.dsl.simulator.Executor;

import com.dsl.simulator.Orekit.SatellitePropagation;


public class SatOpsExecutor {

    private final SatellitePropagation propagation;

    public SatOpsExecutor() {
        this.propagation = new SatellitePropagation();
    }


    public void deploySatellite(String name) {
        System.out.println("Deploying satellite: " + name);
        // you could extend this to keep track of deployed satellites
    }

    //move satellite
    public void moveSatellite(String name, int x, int y) {
        System.out.println("Moving satellite " + name + " to coordinates (" + x + ", " + y + ")");
        // later you can connect this with Orekit for maneuvers
    }

    //print message
    public void printMessage(String message) {
        System.out.println("PRINT: " + message);
    }

    //ruin using orekit
    public void executeSimulateOrbit(double sma, double ecc, double inc) {
        System.out.println("Executing orbit simulation with parameters:");
        System.out.println("  Semi-major axis: " + sma);
        System.out.println("  Eccentricity: " + ecc);
        System.out.println("  Inclination: " + inc);

        propagation.simulateOrbit(sma, ecc, inc);
    }
}
