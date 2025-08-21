package com.dsl.simulator.Product;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GroundStation {
    private String name;
    private double latitude;
    private double longitude;
    public double minElevationDeg = 10.0;

    // satellites this GS is linked to
    public final Set<String> linkedSatellites = new LinkedHashSet<>();
    // simple inbox for messages
    public final List<String> messages = new ArrayList<>();

    public GroundStation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return "GroundStation{Name='" + name + "', Latitude=" + latitude + ", Longitude=" + longitude + "}";
    }
}
