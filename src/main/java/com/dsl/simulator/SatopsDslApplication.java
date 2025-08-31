package com.dsl.simulator;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@SpringBootApplication
public class SatopsDslApplication {

	public static void main(String[] args) {

		// --- 1. Configure Orekit Data (Essential Setup) ---
		// This part is correct and necessary. It loads the physics data
		// needed for Orekit to run calculations.
		try {
			final URL orekitDataUrl = SatopsDslApplication.class.getClassLoader().getResource("orekit-data-main");
			if (orekitDataUrl == null) {
				System.err.println("CRITICAL: Orekit data folder not found in resources!");
				throw new RuntimeException("Orekit data not found");
			}
			final File orekitData = new File(orekitDataUrl.toURI());
			final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
			manager.addProvider(new DirectoryCrawler(orekitData));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to locate Orekit data", e);
		}


		SpringApplication.run(SatopsDslApplication.class, args);
	}
}