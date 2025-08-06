package com.dsl.simulator;

import com.dsl.simulator.Python.PythonBridge;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SatopsDslApplication {

	public static void main(String[] args) {
		SpringApplication.run(SatopsDslApplication.class, args);


		String response = PythonBridge.callSayHello("ISRO");
		System.out.println("Python says: " + response);
	}
}
