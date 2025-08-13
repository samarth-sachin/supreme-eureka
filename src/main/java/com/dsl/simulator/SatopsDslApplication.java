package com.dsl.simulator;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

@SpringBootApplication
public class SatopsDslApplication {

	public static void main(String[] args) throws Exception {


		URL orekitDataUrl = SatopsDslApplication.class.getClassLoader().getResource("orekit-data-main");
		if (orekitDataUrl == null) {
			throw new RuntimeException("Orekit data folder not found in resources!");
		}

		File orekitData = Paths.get(orekitDataUrl.toURI()).toFile();

		DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
		manager.addProvider(new DirectoryCrawler(orekitData));

		SpringApplication.run(SatopsDslApplication.class, args);
	}
}
