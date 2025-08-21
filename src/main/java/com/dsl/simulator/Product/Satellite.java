package com.dsl.simulator.Product;

import org.orekit.propagation.analytical.KeplerianPropagator;

import java.util.LinkedHashSet;
import java.util.Set;

public class Satellite {
    private String satelliteName;
    private int x;
    private int y;
    private KeplerianPropagator propagator;

    public Satellite(String satelliteName, KeplerianPropagator prop) {
        this.satelliteName = satelliteName;
        this.propagator = prop;
    }

    public KeplerianPropagator getKeplerianPropagator() {
        return propagator;
    }

        // stations this satellite is linked to
    public final Set<String> linkedStations = new LinkedHashSet<>();

    public Satellite() {}

    public Satellite(String satelliteName, int x, int y) {
        this.satelliteName = satelliteName;
        this.x = x;
        this.y = y;
    }

    public String getSatelliteName() { return satelliteName; }
    public void setSatelliteName(String satelliteName) { this.satelliteName = satelliteName; }

    public int getX() { return x; }
    public int getY() { return y; }

    // keep method name exactly as used in your visitor
    public void setpostion(int x, int y) {
        this.x = x;
        this.y = y;
    }
    // optional correctly spelled alias (not used by visitor but handy)
    public void setPosition(int x, int y) { setpostion(x, y); }

    @Override
    public String toString() {
        return "Satellite{satelliteName='" + satelliteName + "', x=" + x + ", y=" + y + "}";
    }
}
