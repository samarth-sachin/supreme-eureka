package com.dsl.simulator.Product;

import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;

import java.util.LinkedHashSet;
import java.util.Set;

public class Satellite {
    private String satelliteName;
    private String id;  // ADD THIS
    private Integer noradId;  // ADD THIS

    // TLE Data - ADD THESE
    private String tleLine1;
    private String tleLine2;

    // Position data - ADD THESE
    private Double latitude;
    private Double longitude;
    private Double altitude;

    // Dummy coordinates mode (kept for backwards compatibility)
    private Integer x;
    private Integer y;

    // Physics mode (preferred): generic Orekit propagator
    private Propagator propagator;
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
        this.currentDate = null;
    }

    // ---- ADD THESE GETTERS ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getNoradId() { return noradId; }
    public void setNoradId(Integer noradId) { this.noradId = noradId; }

    public String getTleLine1() { return tleLine1; }
    public void setTleLine1(String tleLine1) { this.tleLine1 = tleLine1; }

    public String getTleLine2() { return tleLine2; }
    public void setTleLine2(String tleLine2) { this.tleLine2 = tleLine2; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    // ---- Existing Getters/Setters ----
    public String getSatelliteName() { return satelliteName; }
    public void setSatelliteName(String satelliteName) { this.satelliteName = satelliteName; }

    public Integer getX() { return x; }
    public Integer getY() { return y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setpostion(int x, int y) { setPosition(x, y); }

    public Propagator getPropagator() { return propagator; }
    public void setPropagator(Propagator propagator) { this.propagator = propagator; }

    public boolean isPhysicsBased() { return propagator != null; }
    public Boolean getPhysicsBased() { return isPhysicsBased(); }

    public AbsoluteDate getCurrentDate() { return currentDate; }
    public void setCurrentDate(AbsoluteDate currentDate) { this.currentDate = currentDate; }

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
