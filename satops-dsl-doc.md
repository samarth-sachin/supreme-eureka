# SatOps-DSL Project Documentation

## Overview

The **SatOps-DSL (Domain Specific Language for Satellite Operations)**
project integrates **ANTLR4** for parsing satellite-specific commands
and **Orekit** for simulating orbital mechanics.\
It allows users to define simple satellite operations in a DSL and
execute them through Java + Spring Boot.

------------------------------------------------------------------------

## Key Concepts in Orbital Mechanics

### 1. Semi-Major Axis (SMA)

-   Defines the size of the orbit.
-   Measured in kilometers (km).
-   Larger SMA → higher altitude orbit.
-   Example: GPS satellites have SMA ≈ 26,560 km.

### 2. Eccentricity (ECC)

-   Defines the **shape of the orbit**.
-   Range: 0 ≤ e \< 1
    -   e = 0 → Circular orbit
    -   0 \< e \< 1 → Elliptical orbit
-   Example: Earth's orbit around the Sun has e ≈ 0.0167.

### 3. Inclination (INC)

-   Angle between the orbital plane and Earth's equator.
-   Measured in degrees (0°--180°).
-   Types:
    -   0° → Equatorial orbit
    -   90° → Polar orbit
    -   98° → Sun-synchronous orbit (common for Earth observation).

### 4. Mean Anomaly (M)

-   Indicates where the satellite is in its orbit relative to perigee.
-   Expressed in degrees (0°--360°).
-   Helps calculate satellite position at any given time.

### 5. Right Ascension of Ascending Node (RAAN)

-   Angle that locates the orbit's intersection with Earth's equator.
-   Important for defining ground tracks.

### 6. Argument of Perigee (ω)

-   Defines orientation of the orbit inside its plane.

------------------------------------------------------------------------

## Orekit Library

**Orekit** is a Java library for **astrodynamics** and **orbit
propagation**.\
We use it to: - Build Earth and reference frames (`OneAxisEllipsoid`,
`FramesFactory`). - Define orbits using **Keplerian elements**. -
Propagate orbits with `KeplerianPropagator`. - Compute satellite
positions and velocities.

**Example Orekit Code:**

``` java
Frame inertialFrame = FramesFactory.getEME2000();
AbsoluteDate date = AbsoluteDate.J2000_EPOCH;

KeplerianOrbit orbit = new KeplerianOrbit(
    sma, ecc, inc,
    0.0, 0.0, 0.0,
    PositionAngle.MEAN,
    inertialFrame, date, Constants.WGS84_EARTH_MU
);
Propagator propagator = new KeplerianPropagator(orbit);
SpacecraftState state = propagator.propagate(date.shiftedBy(600.0));
System.out.println("Satellite Position: " + state.getPVCoordinates().getPosition());
```

------------------------------------------------------------------------

## ANTLR4 Integration

ANTLR4 is used to parse the DSL commands.

### Example DSL Commands:

    deploy sat1
    move sat1 10 20
    print "Mission started"
    simulateOrbit 7000000 0.01 98.5

### Grammar Snippet (SatOps.g4):

``` antlr
program         : statement+ ;
statement       : deployStatement
                | moveStatement
                | printStatement
                | simulateOrbitStatement ;

deployStatement : 'deploy' ID ;
moveStatement   : 'move' ID INT INT ;
printStatement  : 'print' STRING ;
simulateOrbitStatement : 'simulateOrbit' NUMBER NUMBER NUMBER ;
```

ANTLR generates: - **SatOpsLexer.java** (tokenizer) -
**SatOpsParser.java** (parser) - **SatOpsBaseVisitor.java** (visitor
pattern)

------------------------------------------------------------------------

## Execution Flow

1.  User writes a script in SatOps DSL.
2.  ANTLR parses it → AST.
3.  Visitor (`SatOpsVisitorImpl`) processes statements.
4.  `SatOpsExecutor` connects to Orekit and simulates orbit.

------------------------------------------------------------------------

## Example Execution

DSL Input:

    deploy sat1
    simulateOrbit 7000000 0.01 98.5
    print "Orbit simulation complete"

Output:

    Deployed sat1
    Earth model loaded for sat1
    Simulating orbit with parameters: SMA=7000000, ECC=0.01, INC=98.5
    Satellite Position: {x=..., y=..., z=...}
    Message: Orbit simulation complete

------------------------------------------------------------------------

## Advantages

-   Abstracts complex orbital mechanics behind simple DSL commands.
-   Extensible: add new commands like `track`, `telemetry`, `deorbit`.
-   Educational + research use cases.

------------------------------------------------------------------------

## Future Enhancements

-   Add **ground station support** (antenna visibility).
-   Support **TLE (Two-Line Elements)** for real satellite data.
-   Visualization with **CesiumJS** or **Matplotlib (via Python
    bridge)**.
-   Multi-satellite constellations.

------------------------------------------------------------------------

## References

-   Orekit: <https://www.orekit.org>
-   ANTLR4: <https://www.antlr.org>
-   Orbital Mechanics Fundamentals: Curtis, *Orbital Mechanics for
    Engineering Students*
