
package com.dsl.simulator.Service;

import org.orekit.propagation.analytical.tle.TLE;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class CelestrakService {

    private final WebClient webClient;

    public CelestrakService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://celestrak.org/NORAD/elements/gp.php").build();
    }

    /**
     * Fetches the latest TLE for a satellite by its NORAD Catalog Number.
     * For ISS, the NORAD number is 25544.
     */
    public Optional<TLE> fetchTleById(int noradId) {
        try {

            Mono<String> tleStringMono = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("CATNR", noradId)
                            .queryParam("FORMAT", "tle")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class);


            String tleString = tleStringMono.block();


            if (tleString != null && !tleString.isBlank()) {
                String[] lines = tleString.trim().split("\r?\n");
                if (lines.length == 3) {
                    // Orekit's TLE constructor takes line 1 and line 2
                    return Optional.of(new TLE(lines[1].trim(), lines[2].trim()));
                }
            }
        } catch (Exception e) {

            System.err.println("Failed to fetch TLE from Celestrak: " + e.getMessage());
        }
        return Optional.empty();
    }
}