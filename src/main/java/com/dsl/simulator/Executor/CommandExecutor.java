package com.dsl.simulator.Executor;

public class CommandExecutor {

    // Deploy satellite
    public String executeDeploy(String satellite) {
        return "Satellite " + satellite + " deployed successfully.";
    }

    // Move satellite
    public String executeMove(String satellite, int x, int y) {
        return "Satellite " + satellite + " moved to coordinates (" + x + ", " + y + ").";
    }

    // Print message
    public String executePrint(String message) {
        return "[PRINT] " + message;
    }

    // Simulate orbit with Orekit (sma = semi-major axis, ecc = eccentricity, inc = inclination)
    public String executeSimulateOrbit(double sma, double ecc, double inc) {
        // Later you’ll plug in Orekit for real orbit simulation
        return "Simulating orbit with sma=" + sma + ", ecc=" + ecc + ", inc=" + inc + "°";
    }
}
