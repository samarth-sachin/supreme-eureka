package com.dsl.simulator;

import com.dsl.simulator.Orekit.SatellitePropagation;
import com.dsl.simulator.visitor.SatOpsVisitor;
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

		// Load Orekit data from resources
		URL orekitDataUrl = SatopsDslApplication.class.getClassLoader().getResource("orekit-data-main");
		if (orekitDataUrl == null) {
			throw new RuntimeException("Orekit data folder not found in resources!");
		}

		File orekitData = Paths.get(orekitDataUrl.toURI()).toFile();
		DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
		manager.addProvider(new DirectoryCrawler(orekitData));

		// Run satellite propagation simulation
		SatellitePropagation propagation = new SatellitePropagation();
		SatOpsVisitor visitor = new SatOpsVisitor();
		visitor.printAll();

		// Start Spring Boot
		SpringApplication.run(SatopsDslApplication.class, args);
	}
}
//package com.dsl.simulator;
//
//import com.dsl.simulator.visitor.SatOpsVisitor;
//import com.dsl.simulator.SatOpsLexer;
//import com.dsl.simulator.SatOpsParser;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import java.io.InputStream;
//
//@SpringBootApplication
//public class SatopsDslApplication {
//
//	public static void main(String[] args) throws Exception {
//		// Load .satops script from resources
//		InputStream scriptStream = SatopsDslApplication.class.getClassLoader().getResourceAsStream("script.satops");
//		if (scriptStream == null) {
//			throw new RuntimeException("script.satops file not found in resources!");
//		}
//
//		// ANTLR parsing
//		SatOpsLexer lexer = new SatOpsLexer(CharStreams.fromStream(scriptStream));
//		CommonTokenStream tokens = new CommonTokenStream(lexer);
//		SatOpsParser parser = new SatOpsParser(tokens);
//
//		// Run visitor
//		SatOpsVisitor visitor = new SatOpsVisitor();
//		visitor.visit(parser.program());
//
//		// Print memory of satellites after execution
//		visitor.printSatellites();
//
//		// Start Spring Boot normally (optional)
//		SpringApplication.run(SatopsDslApplication.class, args);
//	}
//}
