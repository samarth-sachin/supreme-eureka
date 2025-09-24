package com.dsl.simulator.OptaPlaner;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormationPosition {

    private double x; // meters relative to formation center
    private double y; // meters relative to formation center
    private double z; // meters relative to formation center

    public double distanceFrom(FormationPosition other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    @Override
    public String toString() {
        return String.format("Position(%.2f, %.2f, %.2f)", x, y, z);
    }
}
