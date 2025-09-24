package com.dsl.simulator.OptaPlaner;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvoidanceManeuver {

    private String satelliteId;
    private String threatObjectId;
    private long maneuverTime; // seconds from now
    private double deltaVRequired; // km/s
    private String direction; // "prograde", "retrograde", "radial", "anti-radial"
    private double fuelCost; // kg
    private double successProbability; // 0.0 to 1.0
    private ManeuverType type;

    public enum ManeuverType {
        EMERGENCY_AVOIDANCE,
        PLANNED_AVOIDANCE,
        FORMATION_CORRECTION,
        ORBIT_MAINTENANCE
    }

    @Override
    public String toString() {
        return String.format("Maneuver[%s: %.3f km/s %s in %d seconds]",
                satelliteId, deltaVRequired, direction, maneuverTime);
    }
}
