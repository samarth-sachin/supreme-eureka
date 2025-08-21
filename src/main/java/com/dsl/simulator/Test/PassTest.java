package com.dsl.simulator.Test;

import com.dsl.simulator.Predictor.PassPredictor;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;

import java.util.Optional;

public class PassTest {
    public static void main(String[] args) {
        String line1 = "1 25544U 98067A   24233.51782528  .00016717  00000-0  30664-3 0  9992";
        String line2 = "2 25544  51.6425  45.2136 0005460 129.9056 311.5900 15.50412267423849"; // ISS TLE

        TLE tle = new TLE(line1, line2);
        Propagator propagator = TLEPropagator.selectExtrapolator(tle);

        // Example GS: Bangalore, India
        double lat = 12.9716;
        double lon = 77.5946;

        Optional<String> window = PassPredictor.nextPassWindow(propagator, lat, lon, 10.0, 86400);
        window.ifPresent(System.out::println);
    }
}
