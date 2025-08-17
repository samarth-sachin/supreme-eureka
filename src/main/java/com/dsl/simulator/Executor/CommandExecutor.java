package com.dsl.simulator.Executor;

public class CommandExecutor {

    public String executeDeploy(String sat) {
        return "Deployed " + sat;
    }

    public String executeMove(String sat, double x, double y) {
        return "Moved " + sat + " to (" + x + ", " + y + ")";
    }

    public String executePrint(String message) {
        return "Message: " + message;
    }

    public String executeSimulateOrbit(double sma, double ecc, double inc) {
        return "Simulating orbit with parameters: SMA=" + sma + ", ECC=" + ecc + ", INC=" + inc;
    }
}
