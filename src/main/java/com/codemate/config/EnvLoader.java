package com.codemate.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class EnvLoader {
    private static final String DEFAULT_PROVIDER = "deepseek";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/v1";
    private static final String DEFAULT_PROJECT_CONTEXT = "CODEMATE.md";
    private static final int DEFAULT_MAX_AGENT_STEPS = 8;

    private EnvLoader() {
    }

    public static AppConfig load(Path projectRoot) {
        Map<String, String> dotEnv = readDotEnv(projectRoot.resolve(".env"));

        String provider = get(dotEnv, "CODEMATE_PROVIDER", DEFAULT_PROVIDER);
        String model = get(dotEnv, "CODEMATE_MODEL", DEFAULT_MODEL);
        String baseUrl = get(dotEnv, "CODEMATE_BASE_URL", DEFAULT_BASE_URL);
        String apiKey = get(dotEnv, "CODEMATE_API_KEY", "");
        String projectContext = get(dotEnv, "CODEMATE_PROJECT_CONTEXT", DEFAULT_PROJECT_CONTEXT);
        int maxAgentSteps = parseInt(
                get(dotEnv, "CODEMATE_MAX_AGENT_STEPS", String.valueOf(DEFAULT_MAX_AGENT_STEPS)),
                DEFAULT_MAX_AGENT_STEPS
        );

        return new AppConfig(provider, model, baseUrl, apiKey, projectContext, maxAgentSteps);
    }

    private static String get(Map<String, String> dotEnv, String key, String defaultValue) {
        String environmentValue = System.getenv(key);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String fileValue = dotEnv.get(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        return defaultValue;
    }

    private static Map<String, String> readDotEnv(Path path) {
        Map<String, String> values = new HashMap<>();
        if (!Files.exists(path)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                values.put(key, stripQuotes(value));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env: " + path, e);
        }

        return values;
    }

    private static String stripQuotes(String value) {
        if (value.length() < 2) {
            return value;
        }
        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
        if (doubleQuoted || singleQuoted) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
