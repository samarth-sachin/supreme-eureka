package com.dsl.simulator.Executor;

import com.dsl.simulator.Orekit.SatellitePropagation;

/**
 * Executor class that handles DSL commands and delegates to Orekit simulation or other actions.
 */
public class SatOpsExecutor {

    private final SatellitePropagation propagation;

    public SatOpsExecutor() {
        this.propagation = new SatellitePropagation();
    }

    /**
     * Deploy a satellite into the system
     */
    public void deploySatellite(String name) {
        System.out.println("Deploying satellite: " + name);
        // you could extend this to keep track of deployed satellites
    }

    /**
     * Move satellite to new coordinates (dummy function for now)
     */
    public void moveSatellite(String name, int x, int y) {
        System.out.println("Moving satellite " + name + " to coordinates (" + x + ", " + y + ")");
        // later you can connect this with Orekit for maneuvers
    }

    /**
     * Print a message (DSL `print "..."`)
     */
    public void printMessage(String message) {
        System.out.println("PRINT: " + message);
    }

    /**
     * Run orbit simulation using Orekit
     */
    public void executeSimulateOrbit(double sma, double ecc, double inc) {
        System.out.println("Executing orbit simulation with parameters:");
        System.out.println("  Semi-major axis: " + sma);
        System.out.println("  Eccentricity: " + ecc);
        System.out.println("  Inclination: " + inc);

        propagation.simulateOrbit(sma, ecc, inc);
    }
}
