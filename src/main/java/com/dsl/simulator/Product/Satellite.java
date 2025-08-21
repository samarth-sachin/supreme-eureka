package com.dsl.simulator.Product;

import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;

import java.util.LinkedHashSet;
import java.util.Set;

public class Satellite {
    private String satelliteName;

    // Dummy coordinates mode (kept for backwards compatibility)
    private Integer x;
    private Integer y;

    // Physics mode (preferred): generic Orekit propagator
    private Propagator propagator;

    // Per-satellite time reference for propagation-based commands
    // (defaults to "now" at creation; can be advanced by 'move')
    private AbsoluteDate currentDate;

    // stations this satellite is linked to
    public final Set<String> linkedStations = new LinkedHashSet<>();

    // ---- Constructors ----
    public Satellite() {}

    // Physics-based constructor
    public Satellite(String satelliteName, Propagator propagator, AbsoluteDate epoch) {
        this.satelliteName = satelliteName;
        this.propagator = propagator;
        this.currentDate = epoch;
    }

    // Simple (x,y) constructor
    public Satellite(String satelliteName, int x, int y) {
        this.satelliteName = satelliteName;
        this.x = x;
        this.y = y;
        this.currentDate = null; // no physics time tracking in dummy mode
    }

    // ---- Getters/Setters ----
    public String getSatelliteName() { return satelliteName; }
    public void setSatelliteName(String satelliteName) { this.satelliteName = satelliteName; }

    public Integer getX() { return x; }
    public Integer getY() { return y; }

    // Correctly spelled method
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // (kept only to avoid breaking old visitor code; ideally remove and use setPosition)
    public void setpostion(int x, int y) { setPosition(x, y); }

    public Propagator getPropagator() { return propagator; }
    public void setPropagator(Propagator propagator) { this.propagator = propagator; }

    public boolean isPhysicsBased() { return propagator != null; }

    public AbsoluteDate getCurrentDate() { return currentDate; }
    public void setCurrentDate(AbsoluteDate currentDate) { this.currentDate = currentDate; }

    /** Advance per-satellite time (only meaningful in physics mode) */
    public void advanceTime(double dtSeconds) {
        if (currentDate != null) {
            currentDate = currentDate.shiftedBy(dtSeconds);
        }
    }

    @Override
    public String toString() {
        if (isPhysicsBased()) {
            return "Satellite{satelliteName='" + satelliteName + "', mode=Physics, epoch=" + currentDate + "}";
        } else {
            return "Satellite{satelliteName='" + satelliteName + "', x=" + x + ", y=" + y + "}";
        }
    }
}
