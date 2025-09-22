package com.dsl.simulator.Product;

import org.orekit.bodies.GeodeticPoint;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GroundStation {
    private String name;
    private double latitude;
    private double longitude;
    public double minElevationDeg = 10.0;

    public final Set<String> linkedSatellites = new LinkedHashSet<>();
    public final List<String> messages = new ArrayList<>();

    public GroundStation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    /**
     * NEW METHOD: Returns the ground station's location as an Orekit GeodeticPoint.
     * This is needed for the TargetPointing class.
     */
    public GeodeticPoint getGeodeticPoint() {
        // Orekit uses radians, so we must convert from the degrees we store.
        // Assumes altitude is 0.0 meters.
        return new GeodeticPoint(Math.toRadians(this.latitude), Math.toRadians(this.longitude), 0.0);
    }

    @Override
    public String toString() {
        return "GroundStation{Name='" + name + "', Latitude=" + latitude + ", Longitude=" + longitude + "}";
    }


}

