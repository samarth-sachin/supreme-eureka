grammar SatOps;

program:
    statement+ EOF;

statement:
    // Basic Commands
    deployStatement                     #deployStatementAlt
    | moveStatement                     #moveStatementAlt
    | printStatement                    #printStatementAlt
    | simulateOrbitStatement            #simulateOrbitStatementAlt
    | deployGroundStationStatement      #deployGroundStationStatementAlt
    | linkStatement                     #linkStatementAlt
    | unlinkStatement                   #unlinkStatementAlt
    | sendStatement                     #sendStatementAlt
    | receiveStatement                  #receiveStatementAlt
    | predictPassStatement              #predictPassStatementAlt
    | maneuverStatement                 #maneuverStatementAlt

    // Attitude and Orbit Control
    | setAttitudeStatement              #setAttitudeStatementAlt
    | propagateNumericallyStatement     #propagateNumericallyStatementAlt
    | thrusterFireStatement             #thrusterFireStatementAlt
    | spinControlStatement              #spinControlStatementAlt
    | momentumWheelStatement            #momentumWheelStatementAlt
    | sensorControlStatement            #sensorControlStatementAlt

    // Satellite Deployment
    | separationStatement               #separationStatementAlt
    | solarArrayDeployStatement         #solarArrayDeployStatementAlt
    | antennaDeployStatement            #antennaDeployStatementAlt
    | transponderActivateStatement      #transponderActivateStatementAlt

    // Propulsion System
    | engineBurnStatement               #engineBurnStatementAlt
    | propellantValveStatement          #propellantValveStatementAlt
    | propulsionActivateStatement       #propulsionActivateStatementAlt

    // Payload Operations
    | payloadActivateStatement          #payloadActivateStatementAlt
    | instrumentConfigStatement         #instrumentConfigStatementAlt
    | dataDownlinkStatement             #dataDownlinkStatementAlt

    // Power and Thermal Control
    | batteryManageStatement            #batteryManageStatementAlt
    | heaterControlStatement            #heaterControlStatementAlt
    | radiatorControlStatement          #radiatorControlStatementAlt

    // End-of-Life and Contingency
    | recoveryActionStatement           #recoveryActionStatementAlt
    | decommissionStatement             #decommissionStatementAlt
    | graveyardOrbitStatement           #graveyardOrbitStatementAlt
    | systemShutdownStatement           #systemShutdownStatementAlt

    // Advanced Analysis
    | determineOrbitStatement           #determineOrbitStatementAlt
    | predictEventsStatement            #predictEventsStatementAlt
    | getStatusStatement                #getStatusStatementAlt
    | getGroundStationStatusStatement   #getGroundStationStatusStatementAlt
    | getSystemStatusStatement          #getSystemStatusStatementAlt
    | helpStatement                     #helpStatementAlt

    // GOD-LEVEL COMMANDS
    | propagateUltraPreciseStatement    #propagateUltraPreciseStatementAlt
    | getRealTimeISSStatement           #getRealTimeISSStatementAlt
    | assessCollisionRiskStatement      #assessCollisionRiskStatementAlt
    | getCurrentSpaceWeatherStatement   #getCurrentSpaceWeatherStatementAlt
    | calculateRealTimeDragStatement    #calculateRealTimeDragStatementAlt
    | checkApiHealthStatement           #checkApiHealthStatementAlt
    | getSystemTelemetryStatement       #getSystemTelemetryStatementAlt
    | detectAnomaliesStatement          #detectAnomaliesStatementAlt
    | predictMaintenanceStatement       #predictMaintenanceStatementAlt
    | planDeepSpaceMissionStatement     #planDeepSpaceMissionStatementAlt
    ;

// --- GOD-LEVEL COMMAND DEFINITIONS ---

// Ultra-precise propagation - propagateUltraPrecise iss 24.0;
propagateUltraPreciseStatement:
    'propagateUltraPrecise' ID NUMBER ';';

// Real-time ISS position - getRealTimeISS;
getRealTimeISSStatement:
    'getRealTimeISS' ';';

// Collision risk assessment - assessCollisionRisk iss 72;
assessCollisionRiskStatement:
    'assessCollisionRisk' ID NUMBER ';';

// Current space weather - getCurrentSpaceWeather;
getCurrentSpaceWeatherStatement:
    'getCurrentSpaceWeather' ';';

// Real-time atmospheric drag - calculateRealTimeDrag iss 408.0;
calculateRealTimeDragStatement:
    'calculateRealTimeDrag' ID NUMBER ';';

// API health check - checkApiHealth;
checkApiHealthStatement:
    'checkApiHealth' ';';

// System telemetry - getSystemTelemetry;
getSystemTelemetryStatement:
    'getSystemTelemetry' ';';

// AI anomaly detection - detectAnomalies iss;
detectAnomaliesStatement:
    'detectAnomalies' ID ';';

// Predictive maintenance - predictMaintenance iss 90;
predictMaintenanceStatement:
    'predictMaintenance' ID NUMBER ';';

// Deep space mission planning - planDeepSpaceMission probe mars 2026;
planDeepSpaceMissionStatement:
    'planDeepSpaceMission' ID ID NUMBER ';';

// --- SATELLITE DEPLOYMENT COMMANDS ---

// Example: separate ISS from launcher;
separationStatement:
    'separate' ID 'from' 'launcher' ';';

// Example: deploySolarArray ISS;
solarArrayDeployStatement:
    'deploySolarArray' ID ';';

// Example: deployAntenna ISS primary;
antennaDeployStatement:
    'deployAntenna' ID ('primary' | 'secondary' | 'backup') ';';

// Example: activateTransponder ISS band_s;
transponderActivateStatement:
    'activateTransponder' ID ID ';';

// --- ATTITUDE AND ORBIT CONTROL ---

// Example: setAttitude ISS nadir;
setAttitudeStatement:
    'setAttitude' ID ('nadir' | 'target' ID | 'sun' | 'inertial') ';';

// Example: propagateNumerically ISS 24.0;
propagateNumericallyStatement:
    'propagateNumerically' ID NUMBER ';';

// Example: fireThruster ISS north 5.0 seconds;
thrusterFireStatement:
    'fireThruster' ID ('north' | 'south' | 'east' | 'west' | 'forward' | 'backward') NUMBER 'seconds' ';';

// Example: controlSpin ISS 2.5 rpm;
spinControlStatement:
    'controlSpin' ID NUMBER 'rpm' ';';

// Example: momentumWheel ISS x_axis start;
momentumWheelStatement:
    'momentumWheel' ID ('x_axis' | 'y_axis' | 'z_axis') ('start' | 'stop' | 'adjust' NUMBER) ';';

// Example: activateSensor ISS gyroscope;
sensorControlStatement:
    'activateSensor' ID ('gyroscope' | 'magnetometer' | 'sun_sensor' | 'star_tracker') ';';

// --- PROPULSION SYSTEM ---

// Example: engineBurn ISS apogee_motor 30.0 seconds;
engineBurnStatement:
    'engineBurn' ID ID NUMBER 'seconds' ';';

// Example: propellantValve ISS fuel_line open;
propellantValveStatement:
    'propellantValve' ID ID ('open' | 'close') ';';

// Example: activatePropulsion ISS;
propulsionActivateStatement:
    'activatePropulsion' ID ';';

// --- PAYLOAD OPERATIONS ---

// Example: activatePayload ISS camera; deactivatePayload ISS spectrometer;
payloadActivateStatement:
    ('activatePayload' | 'deactivatePayload') ID ID ';';

// Example: configureInstrument ISS camera resolution 1024;
instrumentConfigStatement:
    'configureInstrument' ID ID ID NUMBER? ';';

// Example: startDataDownlink ISS; stopDataDownlink ISS;
dataDownlinkStatement:
    ('startDataDownlink' | 'stopDataDownlink') ID ';';

// --- POWER AND THERMAL CONTROL ---

// Example: manageBattery ISS charge; manageBattery ISS discharge;
batteryManageStatement:
    'manageBattery' ID ('charge' | 'discharge' | 'monitor') ';';

// Example: heaterControl ISS payload_bay on;
heaterControlStatement:
    'heaterControl' ID ID ('on' | 'off') ';';

// Example: radiatorControl ISS primary extend;
radiatorControlStatement:
    'radiatorControl' ID ('primary' | 'secondary') ('extend' | 'retract') ';';

// --- END-OF-LIFE AND CONTINGENCY ---

// Example: executeRecovery ISS safe_mode;
recoveryActionStatement:
    'executeRecovery' ID ID ';';

// Example: decommission ISS;
decommissionStatement:
    'decommission' ID ';';

// Example: moveToGraveyardOrbit ISS;
graveyardOrbitStatement:
    'moveToGraveyardOrbit' ID ';';

// Example: shutdownSystems ISS;
systemShutdownStatement:
    'shutdownSystems' ID ';';

// --- EXISTING COMMANDS (UNCHANGED) ---

deployStatement:
    'deploy' ID 'with' 'id' NUMBER ';';

moveStatement:
    'move' ID 'to' '(' NUMBER ',' NUMBER ')' ';';

printStatement:
    'print' STRING ';';

simulateOrbitStatement:
    'simulateOrbit' NUMBER NUMBER NUMBER ';';

deployGroundStationStatement:
    'deployGroundStation' ID 'at' '(' NUMBER ',' NUMBER ')' ';';

linkStatement:
    'link' ID 'to' ID ';';

unlinkStatement:
    'unlink' ID 'from' ID ';';

sendStatement:
    'send' ID 'to' ID STRING ';';

receiveStatement:
    'receive' ID 'from' ID ';';

predictPassStatement:
    'predictPass' ID 'over' ID ';';

maneuverStatement:
    'maneuver' ID 'burn' NUMBER 'in' ID 'direction' ';';

// --- ANALYSIS COMMANDS ---

determineOrbitStatement:
    'determineOrbit' ID STRING ';';

predictEventsStatement:
    'predictEvents' ID ('eclipses' | 'nodes') NUMBER ';';

getStatusStatement:
    'getStatus' ID ';';

getGroundStationStatusStatement:
    'getGroundStationStatus' ID ';';

getSystemStatusStatement:
    'getSystemStatus' ';';

helpStatement:
    'help' ID? ';';

// --- LEXER TOKENS ---
ID:     [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER: [0-9]+ ('.' [0-9]+)? ;
STRING: '"' (~["\\] | '\\' .)* '"' ;
WS:     [ \t\r\n]+ -> skip ;
COMMENT: '//' ~[\r\n]* -> skip ;
