// src/main/java/com/dsl/simulator/python/PythonBridge.java
package com.dsl.simulator.Python;

import jep.Interpreter;
import jep.SharedInterpreter;

public class PythonBridge {
    public static String callSayHello(String name) {
        try (Interpreter interp = new SharedInterpreter()) {
            interp.runScript("python-wrapper/satops_orekit.py");  // Relative path from project root
            return interp.getValue("say_hello('" + name + "')", String.class);
        }
    }
}
