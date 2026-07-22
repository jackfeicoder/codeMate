package com.codemate.cli;

import com.codemate.config.AppConfig;
import com.codemate.config.EnvLoader;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        AppConfig config = EnvLoader.load(Path.of("."));

        System.out.println("codeMate CLI");
        System.out.println("Provider: " + config.provider());
        System.out.println("Model: " + config.model());
        System.out.println("API Key: " + config.maskedApiKey());
        System.out.println("Project context: " + config.projectContext());
        System.out.println("Max agent steps: " + config.maxAgentSteps());
    }
}
