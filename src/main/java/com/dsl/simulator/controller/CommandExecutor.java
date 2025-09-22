package com.dsl.simulator.controller;

import com.dsl.simulator.Executor.SatOpsExecutor;

public class CommandExecutor {

    private final SatOpsExecutor executor;

    public CommandExecutor() {
        this.executor = new SatOpsExecutor();
    }

    public void deploySatellite(String id) {
        executor.deploySatellite(id);
    }

    public void moveSatellite(String id, int x, int y) {
        executor.moveSatellite(id, x, y);
    }

    public void printMessage(String msg) {
        executor.printMessage(msg);
    }

    public void simulateOrbit(double sma, double ecc, double inc) {
        executor.executeSimulateOrbit(sma, ecc, inc);
    }
}
