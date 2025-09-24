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

    // Real-Time Data Commands
    | propagateUltraPreciseStatement    #propagateUltraPreciseStatementAlt
    | getRealTimeISSStatement           #getRealTimeISSStatementAlt
    | assessCollisionRiskStatement      #assessCollisionRiskStatementAlt
    | getCurrentSpaceWeatherStatement   #getCurrentSpaceWeatherStatementAlt
    | calculateRealTimeDragStatement    #calculateRealTimeDragStatementAlt
    | checkApiHealthStatement           #checkApiHealthStatementAlt
    | getSystemTelemetryStatement       #getSystemTelemetryStatementAlt
    | predictMaintenanceStatement       #predictMaintenanceStatementAlt
    | planDeepSpaceMissionStatement     #planDeepSpaceMissionStatementAlt

    // Optimization Commands
    | testOptimizerStatement            #testOptimizerStatementAlt
    | optimizeFormationStatement        #optimizeFormationStatementAlt
    | optimizeMissionPlanStatement      #optimizeMissionPlanStatementAlt
    | calculateAvoidanceStatement       #calculateAvoidanceStatementAlt

    // Streaming Commands
    | startStreamStatement              #startStreamStatementAlt
    | stopStreamStatement               #stopStreamStatementAlt
    | publishAlertStatement             #publishAlertStatementAlt

    // AI Analytics Commands
    | runAIAnalysisStatement            #runAIAnalysisStatementAlt
    | detectAnomaliesStatement          #detectAnomaliesStatementAlt
    | monitorRealTimeStatement          #monitorRealTimeStatementAlt
    | generateAIMissionStatement        #generateAIMissionStatementAlt
    | updateMLModelsStatement           #updateMLModelsStatementAlt
    | evaluateModelsStatement           #evaluateModelsStatementAlt
    | predictHealthStatement            #predictHealthStatementAlt
    | analyzePatternsStatement          #analyzePatternsStatementAlt
    | predictOptimalWindowsStatement    #predictOptimalWindowsStatementAlt
    | forecastCollisionRiskStatement    #forecastCollisionRiskStatementAlt
    | generateEmergencyPlanStatement    #generateEmergencyPlanStatementAlt
    | analyzeConstellationStatement     #analyzeConstellationStatementAlt
    ;

// ===================================================================
// COMMAND DEFINITIONS
// ===================================================================

// --- BASIC COMMANDS ---

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

// --- ATTITUDE AND ORBIT CONTROL ---

setAttitudeStatement:
    'setAttitude' ID ('nadir' | 'target' ID | 'sun' | 'inertial') ';';

propagateNumericallyStatement:
    'propagateNumerically' ID NUMBER ';';

thrusterFireStatement:
    'fireThruster' ID ('north' | 'south' | 'east' | 'west' | 'forward' | 'backward') NUMBER 'seconds' ';';

spinControlStatement:
    'controlSpin' ID NUMBER 'rpm' ';';

momentumWheelStatement:
    'momentumWheel' ID ('x_axis' | 'y_axis' | 'z_axis') ('start' | 'stop' | 'adjust' NUMBER) ';';

sensorControlStatement:
    'activateSensor' ID ('gyroscope' | 'magnetometer' | 'sun_sensor' | 'star_tracker') ';';

// --- SATELLITE DEPLOYMENT ---

separationStatement:
    'separate' ID 'from' 'launcher' ';';

solarArrayDeployStatement:
    'deploySolarArray' ID ';';

antennaDeployStatement:
    'deployAntenna' ID ('primary' | 'secondary' | 'backup') ';';

transponderActivateStatement:
    'activateTransponder' ID ID ';';

// --- PROPULSION SYSTEM ---

engineBurnStatement:
    'engineBurn' ID ID NUMBER 'seconds' ';';

propellantValveStatement:
    'propellantValve' ID ID ('open' | 'close') ';';

propulsionActivateStatement:
    'activatePropulsion' ID ';';

// --- PAYLOAD OPERATIONS ---

payloadActivateStatement:
    ('activatePayload' | 'deactivatePayload') ID ID ';';

instrumentConfigStatement:
    'configureInstrument' ID ID ID NUMBER? ';';

dataDownlinkStatement:
    ('startDataDownlink' | 'stopDataDownlink') ID ';';

// --- POWER AND THERMAL CONTROL ---

batteryManageStatement:
    'manageBattery' ID ('charge' | 'discharge' | 'monitor') ';';

heaterControlStatement:
    'heaterControl' ID ID ('on' | 'off') ';';

radiatorControlStatement:
    'radiatorControl' ID ('primary' | 'secondary') ('extend' | 'retract') ';';

// --- END-OF-LIFE AND CONTINGENCY ---

recoveryActionStatement:
    'executeRecovery' ID ID ';';

decommissionStatement:
    'decommission' ID ';';

graveyardOrbitStatement:
    'moveToGraveyardOrbit' ID ';';

systemShutdownStatement:
    'shutdownSystems' ID ';';

// --- ADVANCED ANALYSIS ---

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

// --- REAL-TIME DATA COMMANDS ---

propagateUltraPreciseStatement:
    'propagateUltraPrecise' ID NUMBER ';';

getRealTimeISSStatement:
    'getRealTimeISS' ';';

assessCollisionRiskStatement:
    'assessCollisionRisk' ID NUMBER ';';

getCurrentSpaceWeatherStatement:
    'getCurrentSpaceWeather' ';';

calculateRealTimeDragStatement:
    'calculateRealTimeDrag' ID NUMBER ';';

checkApiHealthStatement:
    'checkApiHealth' ';';

getSystemTelemetryStatement:
    'getSystemTelemetry' ';';

predictMaintenanceStatement:
    'predictMaintenance' ID NUMBER ';';

planDeepSpaceMissionStatement:
    'planDeepSpaceMission' ID ID NUMBER ';';

// --- OPTIMIZATION COMMANDS ---

testOptimizerStatement:
    'testOptimizer' ';';

optimizeFormationStatement:
    'optimizeFormation' '[' idList ']' ID NUMBER ';';

optimizeMissionPlanStatement:
    'optimizeMissionPlan' NUMBER 'hours' ';';

calculateAvoidanceStatement:
    'calculateAvoidance' ID ID ';';

// --- STREAMING COMMANDS ---

startStreamStatement:
    'startStream' ID ';';

stopStreamStatement:
    'stopStream' ID ';';

publishAlertStatement:
    'publishAlert' ID ID STRING ';';

// --- AI ANALYTICS COMMANDS ---

runAIAnalysisStatement:
    'runAIAnalysis' ID ';';

detectAnomaliesStatement:
    'detectAnomalies' ID ';';

monitorRealTimeStatement:
    'monitorRealTime' ID ';';

generateAIMissionStatement:
    'generateAIMission' ID ID NUMBER ';';

updateMLModelsStatement:
    'updateMLModels' ID ';';

evaluateModelsStatement:
    'evaluateModels' ';';

predictHealthStatement:
    'predictHealth' ID ';';

analyzePatternsStatement:
    'analyzePatterns' ID ';';

predictOptimalWindowsStatement:
    'predictOptimalWindows' ID ';';

forecastCollisionRiskStatement:
    'forecastCollisionRisk' ID NUMBER ';';

generateEmergencyPlanStatement:
    'generateEmergencyPlan' ID ';';

analyzeConstellationStatement:
    'analyzeConstellation' ';';

// --- HELPER RULES ---

// Helper rule for list of IDs
idList:
    ID (',' ID)*;

// --- LEXER TOKENS ---

ID:     [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER: [0-9]+ ('.' [0-9]+)? ;
STRING: '"' (~["\\] | '\\' .)* '"' ;
WS:     [ \t\r\n]+ -> skip ;
COMMENT: '//' ~[\r\n]* -> skip ;
