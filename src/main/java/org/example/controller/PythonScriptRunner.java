package org.example.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonScriptRunner {
    private final String pythonExecutable;
    private final String scriptPath;

    public PythonScriptRunner(String pythonExecutable, String scriptPath) {
        this.pythonExecutable = pythonExecutable;
        this.scriptPath = scriptPath;
    }

    public void run() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(pythonExecutable, scriptPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("python " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("PythonScriptRunner::run::" +
                    "python_script_exited_with_error_code " + exitCode);
        }
    }
}

