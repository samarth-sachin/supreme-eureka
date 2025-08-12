package com.dsl.simulator;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SatopsDslApplication {

	public static void main(String[] args) {

		File orekitData = new File("src/main/resources/orekit-data-main");

		DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
		manager.addProvider(new DirectoryCrawler(orekitData));

		SpringApplication.run(SatopsDslApplication.class, args);
	}
}

