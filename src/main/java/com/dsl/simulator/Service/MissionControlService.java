package com.dsl.simulator.Service;

import com.dsl.simulator.Orekit.VisibilityUtil;
import com.dsl.simulator.Predictor.PassPredictor;
import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.Product.Satellite;
import lombok.RequiredArgsConstructor;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LevenbergMarquardtOptimizer;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.attitudes.BodyCenterPointing;
import org.orekit.attitudes.InertialProvider;
import org.orekit.attitudes.NadirPointing;
import org.orekit.attitudes.TargetPointing;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.Ellipsoid;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.estimation.leastsquares.BatchLSEstimator;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.Position;
import org.orekit.forces.ForceModel;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.models.earth.atmosphere.HarrisPriester;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.maneuvers.ImpulseManeuver;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.conversion.DormandPrince853IntegratorBuilder;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.propagation.events.EventsLogger;
import org.orekit.propagation.events.NodeDetector;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MissionControlService {

    private final CelestrakService celestrakService;
    private final Map<String, Satellite> activeSatellites = new HashMap<>();
    private final Map<String, GroundStation> activeGroundStations = new HashMap<>();
    private final List<QueuedMessage> messageQueue = new ArrayList<>();

    // Professional satellite subsystem status tracking
    private final Map<String, SatelliteSubsystems> subsystemStatus = new HashMap<>();

    private static class QueuedMessage {
        final String satId, gsId, text;
        final Instant deliverAt;

        QueuedMessage(String satId, String gsId, String text, Instant deliverAt) {
            this.satId = satId;
            this.gsId = gsId;
            this.text = text;
            this.deliverAt = deliverAt;
        }
    }

    public static class SatelliteSubsystems {
        public boolean solarArraysDeployed = false;
        public boolean primaryAntennaDeployed = false;
        public boolean secondaryAntennaDeployed = false;
        public boolean transponderActive = false;
        public boolean propulsionActive = false;
        public double spinRate = 0.0; // RPM
        public Map<String, Boolean> momentumWheels = new HashMap<>();
        public Map<String, Boolean> sensors = new HashMap<>();
        public Map<String, Boolean> payloads = new HashMap<>();
        public Map<String, String> heaters = new HashMap<>();
        public Map<String, String> radiators = new HashMap<>();
        public double batteryCharge = 85.0; // Percentage
        public boolean separated = false;
        public boolean decommissioned = false;
        public String operationalMode = "NORMAL";

        public SatelliteSubsystems() {
            // Initialize default states
            momentumWheels.put("x_axis", false);
            momentumWheels.put("y_axis", false);
            momentumWheels.put("z_axis", false);
            sensors.put("gyroscope", false);
            sensors.put("magnetometer", false);
            sensors.put("sun_sensor", false);
            sensors.put("star_tracker", false);
            heaters.put("payload_bay", "off");
            heaters.put("battery_bay", "off");
            heaters.put("antenna", "off");
            radiators.put("primary", "retracted");
            radiators.put("secondary", "retracted");
        }
    }

    // --- CORE METHODS (EXISTING) ---
    public Satellite deploySatellite(String satId, int noradId) {
        Optional<TLE> tleOpt = celestrakService.fetchTleById(noradId);
        if (tleOpt.isEmpty()) {
            System.out.println("Celestrak fetch failed. Falling back to local TLE file for " + satId);
            tleOpt = loadTleFromClasspath(satId);
        }

        Satellite sat;
        if (tleOpt.isPresent()) {
            TLE tle = tleOpt.get();
            Propagator prop = TLEPropagator.selectExtrapolator(tle);
            AbsoluteDate epoch = VisibilityUtil.nowUtc();
            sat = new Satellite(satId, prop, epoch);
        } else {
            sat = new Satellite(satId, 0, 0);
        }

        activeSatellites.put(satId, sat);
        subsystemStatus.put(satId, new SatelliteSubsystems());
        return sat;
    }

    public GroundStation deployGroundStation(String gsId, double lat, double lon) {
        GroundStation gs = new GroundStation(gsId, lat, lon);
        activeGroundStations.put(gsId, gs);
        return gs;
    }

    // --- SATELLITE DEPLOYMENT OPERATIONS ---

    public String separateFromLauncher(String satId) {
        if (!activeSatellites.containsKey(satId)) {
            return "Error: Satellite " + satId + " not found.";
        }

        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys.separated) {
            return "Warning: " + satId + " already separated from launcher.";
        }

        subsys.separated = true;
        subsys.operationalMode = "SEPARATED";
        return "SUCCESS: " + satId + " separated from launcher. Autonomous mode initiated.";
    }

    public String deploySolarArray(String satId) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.separated) {
            return "Error: Cannot deploy solar arrays before separation.";
        }

        if (subsys.solarArraysDeployed) {
            return "Warning: Solar arrays already deployed on " + satId + ".";
        }

        subsys.solarArraysDeployed = true;
        subsys.batteryCharge = Math.min(100.0, subsys.batteryCharge + 5.0);
        return "SUCCESS: Solar arrays deployed on " + satId + ". Power generation nominal.";
    }

    public String deployAntenna(String satId, String antennaType) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        switch (antennaType.toLowerCase()) {
            case "primary":
                if (subsys.primaryAntennaDeployed) {
                    return "Warning: Primary antenna already deployed on " + satId + ".";
                }
                subsys.primaryAntennaDeployed = true;
                return "SUCCESS: Primary antenna deployed on " + satId + ". Communications established.";

            case "secondary":
                if (subsys.secondaryAntennaDeployed) {
                    return "Warning: Secondary antenna already deployed on " + satId + ".";
                }
                subsys.secondaryAntennaDeployed = true;
                return "SUCCESS: Secondary antenna deployed on " + satId + ". Backup communications ready.";

            case "backup":
                subsys.primaryAntennaDeployed = true;
                subsys.secondaryAntennaDeployed = true;
                return "SUCCESS: All backup antennas deployed on " + satId + ". Full redundancy achieved.";

            default:
                return "Error: Unknown antenna type '" + antennaType + "'. Use primary, secondary, or backup.";
        }
    }

    public String activateTransponder(String satId, String band) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.primaryAntennaDeployed && !subsys.secondaryAntennaDeployed) {
            return "Error: Cannot activate transponder without deployed antenna.";
        }

        subsys.transponderActive = true;
        return "SUCCESS: " + band.toUpperCase() + " band transponder activated on " + satId + ". Ready for communication.";
    }

    // --- ATTITUDE AND ORBIT CONTROL ---

    public String setAttitudeMode(String satId, String mode, Optional<String> targetId) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null || !sat.isPhysicsBased()) {
            return "Error: Satellite not found or not in physics mode.";
        }

        try {
            AttitudeProvider attitudeProvider;
            Frame inertialFrame = FramesFactory.getEME2000();
            OneAxisEllipsoid earth = new OneAxisEllipsoid(
                    Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING,
                    FramesFactory.getITRF(IERSConventions.IERS_2010, true)
            );

            switch (mode.toLowerCase()) {
                case "nadir":
                    attitudeProvider = new NadirPointing(inertialFrame, earth);
                    break;

                case "target":
                    if (targetId.isEmpty()) {
                        return "Error: Target ID is required for target pointing mode.";
                    }
                    GroundStation targetGs = activeGroundStations.get(targetId.get());
                    if (targetGs == null) {
                        return "Error: Target ground station not found: " + targetId.get();
                    }
                    // FIXED: Use proper TargetPointing with GeodeticPoint
                    try {
                        attitudeProvider = new TargetPointing(
                                inertialFrame,
                                targetGs.getGeodeticPoint(),
                                earth
                        );
                    } catch (Exception ex) {
                        // Fallback to nadir if target pointing fails
                        attitudeProvider = new NadirPointing(inertialFrame, earth);
                        return "Warning: Target pointing failed, using nadir for " + satId;
                    }
                    break;

                case "sun":
                    attitudeProvider = new BodyCenterPointing(inertialFrame, (Ellipsoid) CelestialBodyFactory.getSun());
                    break;

                case "inertial":
                    // FIXED: Use InertialProvider constructor instead of static field
                    attitudeProvider = new InertialProvider(inertialFrame);
                    break;

                default:
                    return "Error: Unknown attitude mode '" + mode + "'. Use nadir, target, sun, or inertial.";
            }

            sat.getPropagator().setAttitudeProvider(attitudeProvider);
            return String.format("SUCCESS: Attitude mode for %s set to %s%s.",
                    satId, mode, targetId.map(id -> " (target: " + id + ")").orElse(""));

        } catch (Exception e) {
            e.printStackTrace();
            return "Error setting attitude: " + e.getMessage();
        }
    }


    public String fireThruster(String satId, String direction, double seconds) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.propulsionActive) {
            return "Error: Propulsion system not active on " + satId + ".";
        }

        // Simulate thruster firing
        double fuelUsed = seconds * 0.1; // kg per second (example)
        return String.format("SUCCESS: %s thruster fired on %s for %.1f seconds. Fuel used: %.2f kg.",
                direction.toUpperCase(), satId, seconds, fuelUsed);
    }

    public String controlSpin(String satId, double rpm) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (rpm < 0 || rpm > 10) {
            return "Error: Invalid spin rate. Must be between 0-10 RPM.";
        }

        subsys.spinRate = rpm;
        return String.format("SUCCESS: Spin rate for %s set to %.1f RPM.", satId, rpm);
    }

    public String controlMomentumWheel(String satId, String axis, String action, Optional<Double> value) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        switch (action.toLowerCase()) {
            case "start":
                subsys.momentumWheels.put(axis, true);
                return String.format("SUCCESS: %s momentum wheel started on %s.", axis.toUpperCase(), satId);

            case "stop":
                subsys.momentumWheels.put(axis, false);
                return String.format("SUCCESS: %s momentum wheel stopped on %s.", axis.toUpperCase(), satId);

            case "adjust":
                if (value.isEmpty()) {
                    return "Error: Adjust value required for momentum wheel adjustment.";
                }
                subsys.momentumWheels.put(axis, true);
                return String.format("SUCCESS: %s momentum wheel adjusted to %.1f RPM on %s.",
                        axis.toUpperCase(), value.get(), satId);

            default:
                return "Error: Unknown action '" + action + "'. Use start, stop, or adjust.";
        }
    }

    public String activateSensor(String satId, String sensorType) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.sensors.containsKey(sensorType)) {
            return "Error: Unknown sensor type '" + sensorType + "'.";
        }

        subsys.sensors.put(sensorType, true);
        return String.format("SUCCESS: %s activated on %s. Telemetry nominal.",
                sensorType.toUpperCase(), satId);
    }

    // --- PROPULSION SYSTEM ---

    public String engineBurn(String satId, String engineId, double seconds) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.propulsionActive) {
            return "Error: Propulsion system not active on " + satId + ".";
        }

        double deltaV = seconds * 0.05; // km/s (example calculation)
        double fuelUsed = seconds * 0.5; // kg (example)

        return String.format("SUCCESS: %s engine burn completed on %s. Duration: %.1fs, ΔV: %.3f km/s, Fuel used: %.1f kg.",
                engineId.toUpperCase(), satId, seconds, deltaV, fuelUsed);
    }

    public String controlPropellantValve(String satId, String valveId, String action) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        return String.format("SUCCESS: %s valve %s on %s.",
                valveId.toUpperCase(), action.toLowerCase(), satId);
    }

    public String activatePropulsion(String satId) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (subsys.propulsionActive) {
            return "Warning: Propulsion system already active on " + satId + ".";
        }

        subsys.propulsionActive = true;
        return "SUCCESS: Propulsion system activated on " + satId + ". All engines ready.";
    }

    // --- PAYLOAD OPERATIONS ---

    public String controlPayload(String satId, String payloadId, String action) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        boolean activate = "activate".equals(action);
        subsys.payloads.put(payloadId, activate);

        return String.format("SUCCESS: %s %s on %s.",
                payloadId.toUpperCase(),
                activate ? "activated" : "deactivated",
                satId);
    }

    public String configureInstrument(String satId, String instrumentId, String parameter, Optional<Double> value) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.payloads.getOrDefault(instrumentId, false)) {
            return "Warning: " + instrumentId + " not activated on " + satId + ". Activating now.";
        }

        String valueStr = value.map(v -> " to " + v).orElse("");
        return String.format("SUCCESS: %s %s configured%s on %s.",
                instrumentId.toUpperCase(), parameter, valueStr, satId);
    }

    public String controlDataDownlink(String satId, String action) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null) return "Error: Satellite " + satId + " not found.";

        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (!subsys.transponderActive) {
            return "Error: Transponder not active on " + satId + ". Cannot start downlink.";
        }

        boolean starting = "start".equals(action);
        return String.format("SUCCESS: Data downlink %s on %s.",
                starting ? "started" : "stopped", satId);
    }

    // --- POWER AND THERMAL CONTROL ---

    public String manageBattery(String satId, String action) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        switch (action.toLowerCase()) {
            case "charge":
                if (!subsys.solarArraysDeployed) {
                    return "Warning: Solar arrays not deployed. Limited charging capability.";
                }
                subsys.batteryCharge = Math.min(100.0, subsys.batteryCharge + 10.0);
                break;

            case "discharge":
                subsys.batteryCharge = Math.max(0.0, subsys.batteryCharge - 5.0);
                break;

            case "monitor":
                return String.format("Battery status for %s: %.1f%% charge remaining.",
                        satId, subsys.batteryCharge);
        }

        return String.format("SUCCESS: Battery %s completed on %s. Current charge: %.1f%%.",
                action, satId, subsys.batteryCharge);
    }

    public String controlHeater(String satId, String heaterId, String action) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        subsys.heaters.put(heaterId, action.toLowerCase());
        return String.format("SUCCESS: %s heater turned %s on %s.",
                heaterId.toUpperCase(), action.toLowerCase(), satId);
    }

    public String controlRadiator(String satId, String radiatorId, String action) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        subsys.radiators.put(radiatorId, action.toLowerCase());
        return String.format("SUCCESS: %s radiator %s on %s.",
                radiatorId.toUpperCase(), action.toLowerCase(), satId);
    }

    // --- END-OF-LIFE AND CONTINGENCY ---

    public String executeRecovery(String satId, String recoveryMode) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        subsys.operationalMode = recoveryMode.toUpperCase();

        switch (recoveryMode.toLowerCase()) {
            case "safe_mode":
                // Turn off non-essential systems
                subsys.payloads.replaceAll((k, v) -> false);
                return "SUCCESS: " + satId + " entered safe mode. Non-essential systems disabled.";

            case "emergency":
                subsys.operationalMode = "EMERGENCY";
                return "SUCCESS: Emergency recovery initiated on " + satId + ".";

            default:
                return "SUCCESS: Recovery mode '" + recoveryMode + "' executed on " + satId + ".";
        }
    }

    public String decommissionSatellite(String satId) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        if (subsys.decommissioned) {
            return "Warning: " + satId + " already decommissioned.";
        }

        subsys.decommissioned = true;
        subsys.operationalMode = "DECOMMISSIONED";
        return "SUCCESS: " + satId + " decommissioned. End-of-life procedures initiated.";
    }

    public String moveToGraveyardOrbit(String satId) {
        Satellite sat = activeSatellites.get(satId);
        SatelliteSubsystems subsys = subsystemStatus.get(satId);

        if (sat == null || subsys == null) return "Error: Satellite " + satId + " not found.";

        if (!subsys.propulsionActive) {
            return "Error: Propulsion system not active. Cannot execute graveyard orbit maneuver.";
        }

        subsys.operationalMode = "GRAVEYARD_ORBIT";
        return "SUCCESS: " + satId + " moved to graveyard orbit. Disposal maneuver completed.";
    }

    public String shutdownSystems(String satId) {
        SatelliteSubsystems subsys = subsystemStatus.get(satId);
        if (subsys == null) return "Error: Satellite " + satId + " not found.";

        // Shutdown all systems
        subsys.transponderActive = false;
        subsys.propulsionActive = false;
        subsys.payloads.replaceAll((k, v) -> false);
        subsys.momentumWheels.replaceAll((k, v) -> false);
        subsys.sensors.replaceAll((k, v) -> false);
        subsys.heaters.replaceAll((k, v) -> "off");
        subsys.operationalMode = "SHUTDOWN";

        return "SUCCESS: All systems shutdown on " + satId + ". Satellite is now inactive.";
    }

    // --- EXISTING METHODS (KEEP ALL) ---
    public Satellite moveSatellite(String satId, double val1, double val2) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null) throw new IllegalArgumentException("Satellite not found: " + satId);

        if (sat.isPhysicsBased()) {
            sat.advanceTime(val1);
        } else {
            sat.setPosition((int) val1, (int) val2);
        }
        return sat;
    }

    public String executeManeuver(String satId, double deltaV_kms, String direction) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null || !sat.isPhysicsBased()) {
            return "Error: Satellite not found or not in physics mode.";
        }

        try {
            Orbit initialOrbit = sat.getPropagator().getInitialState().getOrbit();
            AbsoluteDate maneuverDate = sat.getCurrentDate();

            Vector3D burnDirection;
            if ("prograde".equalsIgnoreCase(direction)) {
                PVCoordinates pv = initialOrbit.getPVCoordinates(FramesFactory.getEME2000());
                burnDirection = pv.getVelocity().normalize();
            } else if ("retrograde".equalsIgnoreCase(direction)) {
                PVCoordinates pv = initialOrbit.getPVCoordinates(FramesFactory.getEME2000());
                burnDirection = pv.getVelocity().normalize().negate();
            } else {
                return "Error: Unknown maneuver direction '" + direction + "'. Use 'prograde' or 'retrograde'.";
            }

            DateDetector trigger = new DateDetector(maneuverDate).withHandler(new ContinueOnEvent<>());
            ImpulseManeuver<DateDetector> maneuverHandler =
                    new ImpulseManeuver<>(trigger, burnDirection, deltaV_kms * 1000.0);

            KeplerianPropagator newPropagator = new KeplerianPropagator(initialOrbit);
            newPropagator.addEventDetector(maneuverHandler);

            SpacecraftState finalState = newPropagator.propagate(maneuverDate.shiftedBy(10.0));

            sat.setPropagator(new KeplerianPropagator(finalState.getOrbit()));
            sat.setCurrentDate(finalState.getDate());

            return String.format("Maneuver successful for %s. Delta-V: %.3f km/s %s. New orbit calculated.",
                    satId, deltaV_kms, direction);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during maneuver calculation: " + e.getMessage();
        }
    }

    public String propagateNumerically(String satId, double hours) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null || !sat.isPhysicsBased()) {
            return "Error: Satellite not found or not in physics mode.";
        }

        try {
            Orbit initialOrbit = sat.getPropagator().getInitialState().getOrbit();
            SpacecraftState initialState = new SpacecraftState(initialOrbit, 1000.0);

            DormandPrince853IntegratorBuilder integratorBuilder =
                    new DormandPrince853IntegratorBuilder(0.001, 1000.0, 1.0);

            NumericalPropagator propagator = new NumericalPropagator(
                    integratorBuilder.buildIntegrator(initialOrbit, initialOrbit.getType())
            );

            Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            OneAxisEllipsoid earth = new OneAxisEllipsoid(
                    Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING,
                    earthFrame
            );

            ForceModel gravity = new HolmesFeatherstoneAttractionModel(
                    earthFrame,
                    GravityFieldFactory.getNormalizedProvider(10, 10)
            );

            ForceModel drag = new DragForce(
                    new HarrisPriester(CelestialBodyFactory.getSun(), earth),
                    new IsotropicDrag(2.0, 1.0)
            );

            ForceModel thirdBodySun = new ThirdBodyAttraction(CelestialBodyFactory.getSun());
            ForceModel thirdBodyMoon = new ThirdBodyAttraction(CelestialBodyFactory.getMoon());

            propagator.addForceModel(gravity);
            propagator.addForceModel(drag);
            propagator.addForceModel(thirdBodySun);
            propagator.addForceModel(thirdBodyMoon);
            propagator.setInitialState(initialState);

            AbsoluteDate finalDate = initialState.getDate().shiftedBy(hours * 3600.0);
            SpacecraftState finalState = propagator.propagate(finalDate);

            sat.setPropagator(new KeplerianPropagator(finalState.getOrbit()));
            sat.setCurrentDate(finalState.getDate());

            return String.format("Propagated %s numerically for %.2f hours. New altitude: %.2f km",
                    satId, hours, finalState.getOrbit().getA() / 1000.0 - Constants.WGS84_EARTH_EQUATORIAL_RADIUS / 1000.0);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during numerical propagation: " + e.getMessage();
        }
    }

    public String determineOrbit(String satId, String measurementFile) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null || !sat.isPhysicsBased()) {
            return "Error: Satellite not found or not in physics mode.";
        }

        try {
            final ObservableSatellite obsSat = new ObservableSatellite(0);
            final List<ObservedMeasurement<?>> measurements = new ArrayList<>();
            Orbit initialOrbit = sat.getPropagator().getInitialState().getOrbit();

            for (int i = 0; i < 10; i++) {
                AbsoluteDate date = initialOrbit.getDate().shiftedBy(i * 60.0);
                PVCoordinates pv = initialOrbit.getPVCoordinates(date, FramesFactory.getEME2000());

                Vector3D noisyPosition = pv.getPosition().add(
                        new Vector3D(
                                (Math.random() - 0.5) * 20.0,
                                (Math.random() - 0.5) * 20.0,
                                (Math.random() - 0.5) * 20.0
                        )
                );

                measurements.add(new Position(date, noisyPosition, 10.0, 1.0, obsSat));
            }

            DormandPrince853IntegratorBuilder integratorBuilder =
                    new DormandPrince853IntegratorBuilder(0.1, 300, 1.0);
            NumericalPropagatorBuilder propBuilder = new NumericalPropagatorBuilder(
                    initialOrbit,
                    integratorBuilder,
                    PositionAngle.MEAN,
                    1.0
            );

            BatchLSEstimator estimator = new BatchLSEstimator(
                    new LevenbergMarquardtOptimizer(),
                    propBuilder
            );

            measurements.forEach(estimator::addMeasurement);
            estimator.setParametersConvergenceThreshold(1.0e-3);
            estimator.setMaxIterations(20);

            Propagator[] estimatedPropagators = estimator.estimate();
            Orbit newOrbit = estimatedPropagators[0].getInitialState().getOrbit();

            sat.setPropagator(estimatedPropagators[0]);

            return String.format("Orbit determination for %s complete. Estimated altitude: %.2f km",
                    satId, newOrbit.getA() / 1000.0 - Constants.WGS84_EARTH_EQUATORIAL_RADIUS / 1000.0);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during orbit determination: " + e.getMessage();
        }
    }

    public List<String> predictAdvancedEvents(String satId, String eventType, double hours) {
        Satellite sat = activeSatellites.get(satId);
        if (sat == null || !sat.isPhysicsBased()) {
            return List.of("Error: Satellite not found or not in physics mode.");
        }

        try {
            EventsLogger logger = new EventsLogger();
            Propagator originalPropagator = sat.getPropagator();
            Propagator tempPropagator;

            if (originalPropagator instanceof TLEPropagator) {
                TLEPropagator tleProp = (TLEPropagator) originalPropagator;
                tempPropagator = TLEPropagator.selectExtrapolator(tleProp.getTLE());
            } else if (originalPropagator instanceof KeplerianPropagator) {
                KeplerianPropagator kepProp = (KeplerianPropagator) originalPropagator;
                tempPropagator = new KeplerianPropagator(kepProp.getInitialState().getOrbit());
            } else if (originalPropagator instanceof NumericalPropagator) {
                Orbit orbit = originalPropagator.getInitialState().getOrbit();
                tempPropagator = new KeplerianPropagator(orbit);
            } else {
                return List.of("Error: Unsupported propagator type for event prediction.");
            }

            tempPropagator.resetInitialState(originalPropagator.getInitialState());

            if ("eclipses".equalsIgnoreCase(eventType)) {
                OneAxisEllipsoid earth = new OneAxisEllipsoid(
                        Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                        Constants.WGS84_EARTH_FLATTENING,
                        FramesFactory.getITRF(IERSConventions.IERS_2010, true)
                );

                EclipseDetector detector = new EclipseDetector(
                        CelestialBodyFactory.getSun(),
                        Constants.SUN_RADIUS,
                        earth
                ).withHandler(new ContinueOnEvent<>());

                tempPropagator.addEventDetector(logger.monitorDetector(detector));

            } else if ("nodes".equalsIgnoreCase(eventType)) {
                NodeDetector detector = new NodeDetector(
                        tempPropagator.getInitialState().getOrbit(),
                        FramesFactory.getEME2000()
                ).withHandler(new ContinueOnEvent<>());

                tempPropagator.addEventDetector(logger.monitorDetector(detector));

            } else {
                return List.of("Error: Unknown event type '" + eventType + "'. Use 'eclipses' or 'nodes'.");
            }

            AbsoluteDate startDate = sat.getCurrentDate();
            AbsoluteDate endDate = startDate.shiftedBy(hours * 3600.0);
            tempPropagator.propagate(startDate, endDate);

            List<String> results = new ArrayList<>();
            for (EventsLogger.LoggedEvent event : logger.getLoggedEvents()) {
                String eventTime = event.getState().getDate().toString();
                String direction = event.isIncreasing() ? "entry/ascending" : "exit/descending";
                results.add(String.format("%s %s at %s (UTC)", eventType, direction, eventTime));
            }

            if (results.isEmpty()) {
                results.add("No " + eventType + " events found in the next " + hours + " hours.");
            }

            return results;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error predicting events: " + e.getMessage());
        }
    }

    // Communication methods and other existing methods remain the same...
    public String link(String satId, String gsId) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null) {
            return "Error: Cannot link, objects not found.";
        }
        if (!sat.isPhysicsBased()) {
            return "Link failed: Satellite " + satId + " is in dummy mode.";
        }

        boolean isVisible = VisibilityUtil.isVisibleAt(
                sat.getPropagator(),
                sat.getCurrentDate(),
                gs.getLatitude(),
                gs.getLongitude(),
                gs.minElevationDeg
        );

        if (isVisible) {
            sat.linkedStations.add(gsId);
            gs.linkedSatellites.add(satId);
            return "Linked (visible): " + satId + " <-> " + gsId;
        } else {
            return "Not visible now: " + satId + " -> " + gsId;
        }
    }

    public String unlink(String satId, String gsId) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null) {
            return "Error: Cannot unlink, objects not found.";
        }
        sat.linkedStations.remove(gsId);
        gs.linkedSatellites.remove(satId);
        return "Unlinked " + satId + " from " + gsId;
    }

    public String sendMessage(String satId, String gsId, String message) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null) {
            return "Error: Cannot send message (objects not found).";
        }
        if (!sat.linkedStations.contains(gsId)) {
            return "Error: Cannot send message — no active link.";
        }

        Instant deliverAt = Instant.now();
        messageQueue.add(new QueuedMessage(satId, gsId, message, deliverAt));
        return String.format("Queued message from %s to %s: \"%s\"", satId, gsId, message);
    }

    public List<String> receiveMessages(String gsId, String satId) {
        GroundStation gs = activeGroundStations.get(gsId);
        if (gs == null) {
            return List.of("Error: Ground station " + gsId + " not found.");
        }

        Instant now = Instant.now();
        List<String> deliveredMessages = new ArrayList<>();
        Iterator<QueuedMessage> it = messageQueue.iterator();

        while (it.hasNext()) {
            QueuedMessage qm = it.next();
            if (qm.gsId.equals(gsId) && qm.satId.equals(satId) && !now.isBefore(qm.deliverAt)) {
                deliveredMessages.add("From " + satId + ": " + qm.text);
                it.remove();
            }
        }
        return deliveredMessages;
    }

    public Optional<String> predictPass(String satId, String gsId) {
        Satellite sat = activeSatellites.get(satId);
        GroundStation gs = activeGroundStations.get(gsId);
        if (sat == null || gs == null || !sat.isPhysicsBased()) {
            return Optional.of("Cannot predict pass: Satellite or GS not found or not in physics mode.");
        }
        return PassPredictor.nextPassWindow(
                sat.getPropagator(),
                gs.getLatitude(),
                gs.getLongitude(),
                gs.minElevationDeg,
                86400
        );
    }

    public String simulateOrbit(double smaMeters, double ecc, double incDegrees) {
        try {
            List<String> logs = new ArrayList<>();
            Frame inertialFrame = FramesFactory.getEME2000();
            AbsoluteDate initialDate = new AbsoluteDate(2025, 1, 1, 12, 0, 0.0, TimeScalesFactory.getUTC());

            Orbit initialOrbit = new KeplerianOrbit(
                    smaMeters, ecc, Math.toRadians(incDegrees), 0.0, 0.0, 0.0,
                    PositionAngle.MEAN, inertialFrame, initialDate, Constants.EIGEN5C_EARTH_MU
            );

            KeplerianPropagator propagator = new KeplerianPropagator(initialOrbit);

            SpacecraftState state10min = propagator.propagate(initialDate.shiftedBy(600));
            PVCoordinates pv10min = state10min.getPVCoordinates();

            logs.add(String.format("Initial Orbit - SMA: %.2f km, Ecc: %.4f, Inc: %.2f°",
                    smaMeters / 1000.0, ecc, incDegrees));
            logs.add(String.format("T+10min Position (km): (%.2f, %.2f, %.2f)",
                    pv10min.getPosition().getX() / 1000,
                    pv10min.getPosition().getY() / 1000,
                    pv10min.getPosition().getZ() / 1000));
            logs.add(String.format("T+10min Velocity (km/s): (%.3f, %.3f, %.3f)",
                    pv10min.getVelocity().getX() / 1000,
                    pv10min.getVelocity().getY() / 1000,
                    pv10min.getVelocity().getZ() / 1000));

            return String.join("\n", logs);

        } catch (Exception e) {
            e.printStackTrace();
            return "Orbit simulation failed: " + e.getMessage();
        }
    }

    // Status and utility methods
    public Map<String, Satellite> getActiveSatellites() {
        return new HashMap<>(activeSatellites);
    }

    public Map<String, GroundStation> getActiveGroundStations() {
        return new HashMap<>(activeGroundStations);
    }

    public SatelliteSubsystems getSubsystemStatus(String satId) {
        return subsystemStatus.get(satId);
    }

    public boolean isSatelliteActive(String satId) {
        return activeSatellites.containsKey(satId);
    }

    public boolean isGroundStationActive(String gsId) {
        return activeGroundStations.containsKey(gsId);
    }

    private Optional<TLE> loadTleFromClasspath(String satId) {
        String path = "/tle/" + satId + ".tle";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return Optional.empty();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String l1 = br.readLine();
                String l2 = br.readLine();
                if (l1 != null && l2 != null && l1.startsWith("1 ") && l2.startsWith("2 ")) {
                    return Optional.of(new TLE(l1.trim(), l2.trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load TLE from classpath: " + path + " - " + e.getMessage());
        }
        return Optional.empty();
    }
}
