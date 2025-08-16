package com.dsl.simulator.Orekit;

import org.orekit.data.*;
import java.io.File;

public class OrekitInitializer {
    public static void initialize() {
        File orekitData = new File("src/main/resources/orekit-data-main");
        DataProvidersManager manager = new DataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
    }
}
