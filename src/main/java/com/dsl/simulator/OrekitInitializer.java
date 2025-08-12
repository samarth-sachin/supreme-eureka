package com.dsl.simulator;

import org.orekit.data.*;
import java.io.File;

public class OrekitInitializer {
    public static void initialize() {
        File orekitData = new File("src/main/resources/orekit-data");
        DataProvidersManager manager = new DataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
    }
}
