package com.dsl.simulator;

import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SatopsDslApplication {

	public static void main(String[] args) {
		// Initialize Orekit data directory
		File orekitData = new File("src/main/resources/orekit-data");
		DataProvidersManager manager = DataProvidersManager.getInstance();
		manager.addProvider(new DirectoryCrawler(orekitData));

		// Start Spring Boot app
		SpringApplication.run(SatopsDslApplication.class, args);
	}
}
