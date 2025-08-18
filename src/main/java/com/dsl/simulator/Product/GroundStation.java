package com.dsl.simulator.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroundStation {
    private String Name;
    private double Latitude;
    private double Longitude;
    public Set<String> linkedSatellites = new HashSet<>();
    public List<String> messages = new ArrayList<>();
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }
public GroundStation(String name, double latitude, double longitude) {
        this.Name = name;
        this.Latitude = latitude;
        this.Longitude = longitude;
}

    @Override
    public String toString() {
        return "GroundStation{" +
                "Name='" + Name + '\'' +
                ", Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                '}';
    }
}
