package com.dsl.simulator.Service;

import com.dsl.simulator.Orekit.SatellitePropagation;
import com.dsl.simulator.Product.GroundStation;
import com.dsl.simulator.Product.Satellite;
import com.dsl.simulator.SatOpsBaseVisitor;
import com.dsl.simulator.SatOpsParser;
import com.dsl.simulator.Service.MissionControlService.SatelliteSubsystems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SatOpsVisitor extends SatOpsBaseVisitor<Void> {

    private final MissionControlService missionControlService;
    private final List<String> logs = new ArrayList<>();
    private SatellitePropagation satellitePropagation = new SatellitePropagation();

    public SatOpsVisitor(MissionControlService missionControlService) {
        this.missionControlService = missionControlService;
    }

    public List<String> getLogs() {
        return logs;
    }

    @Override
    public Void visitProgram(SatOpsParser.ProgramContext ctx) {
        ctx.statement().forEach(this::visit);
        return null;
    }

    // --- EXISTING BASIC COMMANDS (UNCHANGED) ---

    @Override
    public Void visitDeployStatement(SatOpsParser.DeployStatementContext ctx) {
        String satId = ctx.ID().getText();
        int noradId = Integer.parseInt(ctx.NUMBER().getText());
        Satellite sat = missionControlService.deploySatellite(satId, noradId);
        if (sat.isPhysicsBased()) {
            logs.add("✓ DEPLOYED (physics): " + sat.getSatelliteName());
        } else {
            logs.add("⚠ DEPLOYED (dummy): " + sat.getSatelliteName());
            logs.add("  Hint: TLE fetch failed. Check NORAD ID or local file.");
        }
        return null;
    }

    @Override
    public Void visitDeployGroundStationStatement(SatOpsParser.DeployGroundStationStatementContext ctx) {
        String gsId = ctx.ID().getText();
        double lat = Double.parseDouble(ctx.NUMBER(0).getText());
        double lon = Double.parseDouble(ctx.NUMBER(1).getText());
        GroundStation gs = missionControlService.deployGroundStation(gsId, lat, lon);
        logs.add("✓ GROUND STATION DEPLOYED: " + gs.getName() +
                String.format(" (%.4f°, %.4f°)", lat, lon));
        return null;
    }

    @Override
    public Void visitMoveStatement(SatOpsParser.MoveStatementContext ctx) {
        String satId = ctx.ID().getText();
        double val1 = Double.parseDouble(ctx.NUMBER(0).getText());
        double val2 = Double.parseDouble(ctx.NUMBER(1).getText());
        try {
            missionControlService.moveSatellite(satId, val1, val2);
            logs.add("✓ MOVED: " + satId + String.format(" to (%.1f, %.1f)", val1, val2));
        } catch (IllegalArgumentException e) {
            logs.add("✗ ERROR: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Void visitLinkStatement(SatOpsParser.LinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String result = missionControlService.link(satId, gsId);
        logs.add("🔗 " + result);
        return null;
    }

    @Override
    public Void visitUnlinkStatement(SatOpsParser.UnlinkStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String result = missionControlService.unlink(satId, gsId);
        logs.add("🔓 " + result);
        return null;
    }

    @Override
    public Void visitSendStatement(SatOpsParser.SendStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        String result = missionControlService.sendMessage(satId, gsId, message);
        logs.add("📡 " + result);
        return null;
    }

    @Override
    public Void visitReceiveStatement(SatOpsParser.ReceiveStatementContext ctx) {
        String gsId = ctx.ID(0).getText();
        String satId = ctx.ID(1).getText();
        List<String> received = missionControlService.receiveMessages(gsId, satId);
        if (received.isEmpty()) {
            logs.add("📭 No messages received at " + gsId + " from " + satId);
        } else {
            logs.add("📬 MESSAGES RECEIVED:");
            received.forEach(msg -> logs.add("   " + msg));
        }
        return null;
    }

    @Override
    public Void visitPredictPassStatement(SatOpsParser.PredictPassStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String gsId = ctx.ID(1).getText();
        String result = missionControlService.predictPass(satId, gsId)
                .orElse("No pass predicted in the next 24h for " + satId + " over " + gsId);
        logs.add("🛰 PASS PREDICTION: " + result);
        return null;
    }

    @Override
    public Void visitPrintStatement(SatOpsParser.PrintStatementContext ctx) {
        String message = ctx.STRING().getText().replaceAll("^\"|\"$", "");
        logs.add("💬 MESSAGE: " + message);
        return null;
    }

    @Override
    public Void visitSimulateOrbitStatement(SatOpsParser.SimulateOrbitStatementContext ctx) {
        double smaKm = Double.parseDouble(ctx.NUMBER(0).getText());
        double ecc = Double.parseDouble(ctx.NUMBER(1).getText());
        double inc = Double.parseDouble(ctx.NUMBER(2).getText());
        logs.add("🌍 === ORBIT SIMULATION RESULTS ===");
        logs.add(satellitePropagation.simulateOrbit(smaKm * 1000, ecc, inc));
        logs.add("=====================================");
        return null;
    }

    @Override
    public Void visitManeuverStatement(SatOpsParser.ManeuverStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        double deltaV = Double.parseDouble(ctx.NUMBER().getText());
        String direction = ctx.ID(1).getText();
        String result = missionControlService.executeManeuver(satId, deltaV, direction);
        logs.add("🚀 MANEUVER: " + result);
        return null;
    }

    // --- SATELLITE DEPLOYMENT OPERATIONS ---

    @Override
    public Void visitSeparationStatement(SatOpsParser.SeparationStatementContext ctx) {
        String satId = ctx.ID().getText();
        String result = missionControlService.separateFromLauncher(satId);
        logs.add("🚀 SEPARATION: " + result);
        return null;
    }

    @Override
    public Void visitSolarArrayDeployStatement(SatOpsParser.SolarArrayDeployStatementContext ctx) {
        String satId = ctx.ID().getText();
        String result = missionControlService.deploySolarArray(satId);
        logs.add("☀️ SOLAR ARRAY: " + result);
        return null;
    }

    @Override
    public Void visitAntennaDeployStatement(SatOpsParser.AntennaDeployStatementContext ctx) {
        String satId = ctx.ID().getText();
        String antennaType = ctx.getText().contains("primary") ? "primary" :
                ctx.getText().contains("secondary") ? "secondary" : "backup";
        String result = missionControlService.deployAntenna(satId, antennaType);
        logs.add("📡 ANTENNA DEPLOY: " + result);
        return null;
    }

    @Override
    public Void visitTransponderActivateStatement(SatOpsParser.TransponderActivateStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String band = ctx.ID(1).getText();
        String result = missionControlService.activateTransponder(satId, band);
        logs.add("📻 TRANSPONDER: " + result);
        return null;
    }

    // --- ATTITUDE AND ORBIT CONTROL ---

    @Override
    public Void visitSetAttitudeStatement(SatOpsParser.SetAttitudeStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String mode;
        Optional<String> targetId = Optional.empty();

        if (ctx.getText().contains("nadir")) {
            mode = "nadir";
        } else if (ctx.getText().contains("target")) {
            mode = "target";
            if (ctx.ID().size() > 1) {
                targetId = Optional.of(ctx.ID(1).getText());
            }
        } else if (ctx.getText().contains("sun")) {
            mode = "sun";
        } else if (ctx.getText().contains("inertial")) {
            mode = "inertial";
        } else {
            logs.add("✗ ERROR: Unknown attitude mode");
            return null;
        }

        String result = missionControlService.setAttitudeMode(satId, mode, targetId);
        logs.add("🧭 ATTITUDE: " + result);
        return null;
    }

    @Override
    public Void visitThrusterFireStatement(SatOpsParser.ThrusterFireStatementContext ctx) {
        String satId = ctx.ID().getText();
        String direction = ctx.getText().contains("north") ? "north" :
                ctx.getText().contains("south") ? "south" :
                        ctx.getText().contains("east") ? "east" :
                                ctx.getText().contains("west") ? "west" :
                                        ctx.getText().contains("forward") ? "forward" : "backward";
        double seconds = Double.parseDouble(ctx.NUMBER().getText());

        String result = missionControlService.fireThruster(satId, direction, seconds);
        logs.add("🔥 THRUSTER: " + result);
        return null;
    }

    @Override
    public Void visitSpinControlStatement(SatOpsParser.SpinControlStatementContext ctx) {
        String satId = ctx.ID().getText();
        double rpm = Double.parseDouble(ctx.NUMBER().getText());
        String result = missionControlService.controlSpin(satId, rpm);
        logs.add("🌀 SPIN CONTROL: " + result);
        return null;
    }

    @Override
    public Void visitMomentumWheelStatement(SatOpsParser.MomentumWheelStatementContext ctx) {
        String satId = ctx.ID().getText();
        String axis = ctx.getText().contains("x_axis") ? "x_axis" :
                ctx.getText().contains("y_axis") ? "y_axis" : "z_axis";
        String action = ctx.getText().contains("start") ? "start" :
                ctx.getText().contains("stop") ? "stop" : "adjust";

        Optional<Double> value = Optional.empty();
        if ("adjust".equals(action) && ctx.NUMBER() != null) {
            value = Optional.of(Double.parseDouble(ctx.NUMBER().getText()));
        }

        String result = missionControlService.controlMomentumWheel(satId, axis, action, value);
        logs.add("⚙️ MOMENTUM WHEEL: " + result);
        return null;
    }

    @Override
    public Void visitSensorControlStatement(SatOpsParser.SensorControlStatementContext ctx) {
        String satId = ctx.ID().getText();
        String sensorType = ctx.getText().contains("gyroscope") ? "gyroscope" :
                ctx.getText().contains("magnetometer") ? "magnetometer" :
                        ctx.getText().contains("sun_sensor") ? "sun_sensor" : "star_tracker";
        String result = missionControlService.activateSensor(satId, sensorType);
        logs.add("🔍 SENSOR: " + result);
        return null;
    }

    // --- PROPULSION SYSTEM ---

    @Override
    public Void visitEngineBurnStatement(SatOpsParser.EngineBurnStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String engineId = ctx.ID(1).getText();
        double seconds = Double.parseDouble(ctx.NUMBER().getText());
        String result = missionControlService.engineBurn(satId, engineId, seconds);
        logs.add("🔥 ENGINE BURN: " + result);
        return null;
    }

    @Override
    public Void visitPropellantValveStatement(SatOpsParser.PropellantValveStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String valveId = ctx.ID(1).getText();
        String action = ctx.getText().contains("open") ? "open" : "close";
        String result = missionControlService.controlPropellantValve(satId, valveId, action);
        logs.add("⛽ VALVE: " + result);
        return null;
    }

    @Override
    public Void visitPropulsionActivateStatement(SatOpsParser.PropulsionActivateStatementContext ctx) {
        String satId = ctx.ID().getText();
        String result = missionControlService.activatePropulsion(satId);
        logs.add("🚀 PROPULSION: " + result);
        return null;
    }

    // --- PAYLOAD OPERATIONS ---

    @Override
    public Void visitPayloadActivateStatement(SatOpsParser.PayloadActivateStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String payloadId = ctx.ID(1).getText();
        String action = ctx.getText().contains("activatePayload") ? "activate" : "deactivate";
        String result = missionControlService.controlPayload(satId, payloadId, action);
        logs.add("📦 PAYLOAD: " + result);
        return null;
    }

    @Override
    public Void visitInstrumentConfigStatement(SatOpsParser.InstrumentConfigStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String instrumentId = ctx.ID(1).getText();
        String parameter = ctx.ID(2).getText();
        Optional<Double> value = Optional.empty();
        if (ctx.NUMBER() != null) {
            value = Optional.of(Double.parseDouble(ctx.NUMBER().getText()));
        }
        String result = missionControlService.configureInstrument(satId, instrumentId, parameter, value);
        logs.add("🔧 INSTRUMENT CONFIG: " + result);
        return null;
    }

    @Override
    public Void visitDataDownlinkStatement(SatOpsParser.DataDownlinkStatementContext ctx) {
        String satId = ctx.ID().getText();
        String action = ctx.getText().contains("startDataDownlink") ? "start" : "stop";
        String result = missionControlService.controlDataDownlink(satId, action);
        logs.add("📥 DATA DOWNLINK: " + result);
        return null;
    }

    // --- POWER AND THERMAL CONTROL ---

    @Override
    public Void visitBatteryManageStatement(SatOpsParser.BatteryManageStatementContext ctx) {
        String satId = ctx.ID().getText();
        String action = ctx.getText().contains("charge") ? "charge" :
                ctx.getText().contains("discharge") ? "discharge" : "monitor";
        String result = missionControlService.manageBattery(satId, action);
        logs.add("🔋 BATTERY: " + result);
        return null;
    }

    @Override
    public Void visitHeaterControlStatement(SatOpsParser.HeaterControlStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String heaterId = ctx.ID(1).getText();
        String action = ctx.getText().contains(" on") ? "on" : "off";
        String result = missionControlService.controlHeater(satId, heaterId, action);
        logs.add("🔥 HEATER: " + result);
        return null;
    }

    @Override
    public Void visitRadiatorControlStatement(SatOpsParser.RadiatorControlStatementContext ctx) {
        String satId = ctx.ID().getText();
        String radiatorId = ctx.getText().contains("primary") ? "primary" : "secondary";
        String action = ctx.getText().contains("extend") ? "extend" : "retract";
        String result = missionControlService.controlRadiator(satId, radiatorId, action);
        logs.add("❄️ RADIATOR: " + result);
        return null;
    }

    // --- END-OF-LIFE AND CONTINGENCY ---

    @Override
    public Void visitRecoveryActionStatement(SatOpsParser.RecoveryActionStatementContext ctx) {
        String satId = ctx.ID(0).getText();
        String recoveryMode = ctx.ID(1).getText();
        String result = missionControlService.executeRecovery(satId, recoveryMode);
        logs.add("🆘 RECOVERY: " + result);
        return null;
    }

    @Override
    public Void visitDecommissionStatement(SatOpsParser.DecommissionStatementContext ctx) {
        String satId = ctx.ID().getText();
        String result = missionControlService.decommissionSatellite(satId);
        logs.add("⚰️ DECOMMISSION: " + result);
        return null;
    }

    @Override
    public Void visitGraveyardOrbitStatement(SatOpsParser.GraveyardOrbitStatementContext ctx) {
        String satId = ctx.ID().getText();
        String result = missionControlService.moveToGraveyardOrbit(satId);
        logs.add("🪦 GRAVEYARD ORBIT: " + result);
        return null;
    }

    @Override
    public Void visitSystemShutdownStatement(SatOpsParser.SystemShutdownStatementContext ctx) {
        String satId = ctx.ID().getText();
        String result = missionControlService.shutdownSystems(satId);
        logs.add("🔌 SYSTEM SHUTDOWN: " + result);
        return null;
    }

    // --- ADVANCED ANALYSIS ---

    @Override
    public Void visitPropagateNumericallyStatement(SatOpsParser.PropagateNumericallyStatementContext ctx) {
        String satId = ctx.ID().getText();
        double hours = Double.parseDouble(ctx.NUMBER().getText());

        logs.add("🧮 === NUMERICAL PROPAGATION RESULTS ===");
        String result = missionControlService.propagateNumerically(satId, hours);
        logs.add(result);
        logs.add("=====================================");
        return null;
    }

    @Override
    public Void visitDetermineOrbitStatement(SatOpsParser.DetermineOrbitStatementContext ctx) {
        String satId = ctx.ID().getText();
        String measurementFile = ctx.STRING().getText().replaceAll("^\"|\"$", "");

        logs.add("📊 === ORBIT DETERMINATION RESULTS ===");
        String result = missionControlService.determineOrbit(satId, measurementFile);
        logs.add(result);
        logs.add("====================================");
        return null;
    }

    @Override
    public Void visitPredictEventsStatement(SatOpsParser.PredictEventsStatementContext ctx) {
        String satId = ctx.ID().getText();
        String eventType = ctx.getText().contains("eclipses") ? "eclipses" : "nodes";
        double hours = Double.parseDouble(ctx.NUMBER().getText());

        logs.add("🔮 === " + eventType.toUpperCase() + " PREDICTIONS FOR " + satId + " ===");
        List<String> events = missionControlService.predictAdvancedEvents(satId, eventType, hours);
        events.forEach(event -> logs.add("   " + event));
        logs.add("==========================================");
        return null;
    }

    // --- STATUS REPORTING ---

    @Override
    public Void visitGetStatusStatement(SatOpsParser.GetStatusStatementContext ctx) {
        String satId = ctx.ID().getText();

        try {
            Satellite sat = missionControlService.getActiveSatellites().get(satId);
            SatelliteSubsystems subsys = missionControlService.getSubsystemStatus(satId);

            if (sat == null) {
                logs.add("✗ ERROR: Satellite " + satId + " not found.");
                return null;
            }

            logs.add("📋 === STATUS FOR " + satId + " ===");
            logs.add("Name: " + sat.getSatelliteName());
            logs.add("Physics-based: " + (sat.isPhysicsBased() ? "Yes" : "No"));

            if (subsys != null) {
                logs.add("Operational Mode: " + subsys.operationalMode);
                logs.add("Separated: " + (subsys.separated ? "Yes" : "No"));
                logs.add("Solar Arrays: " + (subsys.solarArraysDeployed ? "Deployed" : "Stowed"));
                logs.add("Primary Antenna: " + (subsys.primaryAntennaDeployed ? "Deployed" : "Stowed"));
                logs.add("Transponder: " + (subsys.transponderActive ? "Active" : "Inactive"));
                logs.add("Propulsion: " + (subsys.propulsionActive ? "Active" : "Inactive"));
                logs.add("Battery Charge: " + String.format("%.1f%%", subsys.batteryCharge));
                logs.add("Spin Rate: " + String.format("%.1f RPM", subsys.spinRate));

                // Active payloads
                long activePayloads = subsys.payloads.values().stream().mapToLong(active -> active ? 1 : 0).sum();
                logs.add("Active Payloads: " + activePayloads + "/" + subsys.payloads.size());

                // Active sensors
                long activeSensors = subsys.sensors.values().stream().mapToLong(active -> active ? 1 : 0).sum();
                logs.add("Active Sensors: " + activeSensors + "/" + subsys.sensors.size());
            }

            if (sat.isPhysicsBased()) {
                logs.add("Current Time: " + sat.getCurrentDate().toString());
                logs.add("Linked Stations: " + sat.linkedStations.size());
            } else {
                logs.add("Position: (" + sat.getX() + ", " + sat.getY() + ")");
            }
            logs.add("==========================");

        } catch (Exception e) {
            logs.add("✗ ERROR getting status for " + satId + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public Void visitGetGroundStationStatusStatement(SatOpsParser.GetGroundStationStatusStatementContext ctx) {
        String gsId = ctx.ID().getText();

        try {
            GroundStation gs = missionControlService.getActiveGroundStations().get(gsId);
            if (gs == null) {
                logs.add("✗ ERROR: Ground station " + gsId + " not found.");
                return null;
            }

            logs.add("🏢 === STATUS FOR GROUND STATION " + gsId + " ===");
            logs.add("Name: " + gs.getName());
            logs.add("Latitude: " + String.format("%.4f°", gs.getLatitude()));
            logs.add("Longitude: " + String.format("%.4f°", gs.getLongitude()));
            logs.add("Min Elevation: " + gs.minElevationDeg + "°");
            logs.add("Linked Satellites: " + gs.linkedSatellites.size());
            if (!gs.linkedSatellites.isEmpty()) {
                logs.add("Connected to: " + String.join(", ", gs.linkedSatellites));
            }
            logs.add("=====================================");

        } catch (Exception e) {
            logs.add("✗ ERROR getting status for " + gsId + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public Void visitGetSystemStatusStatement(SatOpsParser.GetSystemStatusStatementContext ctx) {
        logs.add("🖥️ === SYSTEM STATUS ===");
        logs.add("Active Satellites: " + missionControlService.getActiveSatellites().size());
        logs.add("Active Ground Stations: " + missionControlService.getActiveGroundStations().size());

        // Count physics-based vs dummy satellites
        long physicsSats = missionControlService.getActiveSatellites().values().stream()
                .filter(Satellite::isPhysicsBased)
                .count();
        long dummySats = missionControlService.getActiveSatellites().size() - physicsSats;

        logs.add("Physics-based Satellites: " + physicsSats);
        logs.add("Dummy Satellites: " + dummySats);

        // Count total links
        int totalLinks = missionControlService.getActiveSatellites().values().stream()
                .mapToInt(sat -> sat.linkedStations.size())
                .sum();
        logs.add("Total Active Links: " + totalLinks);

        // Count operational modes
        logs.add("\n📊 SATELLITE OPERATIONAL MODES:");
        missionControlService.getActiveSatellites().forEach((id, sat) -> {
            SatelliteSubsystems subsys = missionControlService.getSubsystemStatus(id);
            if (subsys != null) {
                logs.add("  " + id + ": " + subsys.operationalMode);
            }
        });

        logs.add("=====================");
        return null;
    }

    @Override
    public Void visitHelpStatement(SatOpsParser.HelpStatementContext ctx) {
        if (ctx.ID() != null) {
            // Specific command help
            String command = ctx.ID().getText();
            logs.add("❓ === HELP FOR '" + command.toUpperCase() + "' ===");
            logs.add(getCommandHelp(command));
            logs.add("===============================");
        } else {
            // General help
            logs.add("📚 === DSL SATOPS COMMAND REFERENCE ===");
            logs.add("🚀 DEPLOYMENT:");
            logs.add("  deploy SAT_ID with id NORAD_ID");
            logs.add("  deployGroundStation GS_ID at (LAT, LON)");
            logs.add("  separate SAT_ID from launcher");
            logs.add("  deploySolarArray SAT_ID");
            logs.add("  deployAntenna SAT_ID primary/secondary/backup");
            logs.add("");
            logs.add("🧭 ATTITUDE & ORBIT:");
            logs.add("  setAttitude SAT_ID nadir/target/sun/inertial");
            logs.add("  fireThruster SAT_ID DIRECTION SECONDS seconds");
            logs.add("  controlSpin SAT_ID RPM rpm");
            logs.add("  momentumWheel SAT_ID AXIS start/stop");
            logs.add("");
            logs.add("🔥 PROPULSION:");
            logs.add("  activatePropulsion SAT_ID");
            logs.add("  engineBurn SAT_ID ENGINE SECONDS seconds");
            logs.add("  propellantValve SAT_ID VALVE open/close");
            logs.add("");
            logs.add("📦 PAYLOAD:");
            logs.add("  activatePayload/deactivatePayload SAT_ID PAYLOAD");
            logs.add("  configureInstrument SAT_ID INSTRUMENT PARAM");
            logs.add("  startDataDownlink/stopDataDownlink SAT_ID");
            logs.add("");
            logs.add("🔋 POWER & THERMAL:");
            logs.add("  manageBattery SAT_ID charge/discharge/monitor");
            logs.add("  heaterControl SAT_ID HEATER on/off");
            logs.add("  radiatorControl SAT_ID primary/secondary extend/retract");
            logs.add("");
            logs.add("⚠️ CONTINGENCY:");
            logs.add("  executeRecovery SAT_ID safe_mode");
            logs.add("  decommission SAT_ID");
            logs.add("  moveToGraveyardOrbit SAT_ID");
            logs.add("  shutdownSystems SAT_ID");
            logs.add("");
            logs.add("📊 ANALYSIS:");
            logs.add("  getStatus SAT_ID");
            logs.add("  getSystemStatus");
            logs.add("  propagateNumerically SAT_ID HOURS");
            logs.add("  predictEvents SAT_ID eclipses/nodes HOURS");
            logs.add("");
            logs.add("Use 'help COMMAND' for detailed help on specific commands");
            logs.add("=====================================");
        }
        return null;
    }

    /**
     * Utility method to print execution logs with professional formatting
     */
    public void printLogs() {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║        DSL SATOPS EXECUTION LOG       ║");
        System.out.println("╚═══════════════════════════════════════╝");
        for (String logEntry : this.logs) {
            System.out.println(logEntry);
        }
        System.out.println("╚═══════════════════════════════════════╝");
    }

    /**
     * Get detailed help for specific commands
     */
    private String getCommandHelp(String command) {
        switch (command.toLowerCase()) {
            case "deploy":
                return "deploy SAT_ID with id NORAD_ID\n" +
                        "Deploy a satellite using TLE data from NORAD catalog.\n" +
                        "Example: deploy ISS with id 25544;";

            case "separate":
                return "separate SAT_ID from launcher\n" +
                        "Separate satellite from launch vehicle and initiate autonomous mode.\n" +
                        "Example: separate ISS from launcher;";

            case "setattitude":
                return "setAttitude SAT_ID MODE [TARGET]\n" +
                        "Set attitude control mode.\n" +
                        "Modes: nadir, target TARGET_ID, sun, inertial\n" +
                        "Examples: setAttitude ISS nadir;\n" +
                        "         setAttitude LANDSAT target GS_NYC;";

            case "firethruster":
                return "fireThruster SAT_ID DIRECTION SECONDS seconds\n" +
                        "Fire thrusters for orbital corrections.\n" +
                        "Directions: north, south, east, west, forward, backward\n" +
                        "Example: fireThruster ISS north 5.0 seconds;";

            case "engineburn":
                return "engineBurn SAT_ID ENGINE SECONDS seconds\n" +
                        "Execute main engine burn for major orbit changes.\n" +
                        "Example: engineBurn ISS apogee_motor 30.0 seconds;";

            case "managebattery":
                return "manageBattery SAT_ID ACTION\n" +
                        "Manage satellite power systems.\n" +
                        "Actions: charge, discharge, monitor\n" +
                        "Example: manageBattery ISS charge;";

            case "executerecovery":
                return "executeRecovery SAT_ID MODE\n" +
                        "Execute emergency recovery procedures.\n" +
                        "Example: executeRecovery ISS safe_mode;";

            case "decommission":
                return "decommission SAT_ID\n" +
                        "Begin end-of-life procedures for satellite.\n" +
                        "Example: decommission OLD_SAT;";

            case "getstatus":
                return "getStatus SAT_ID\n" +
                        "Get comprehensive status report for satellite.\n" +
                        "Shows all subsystems, power, attitude, and health.\n" +
                        "Example: getStatus ISS;";

            default:
                return "No detailed help available for '" + command + "'.\n" +
                        "Use 'help' to see all available commands.";
        }
    }
}
