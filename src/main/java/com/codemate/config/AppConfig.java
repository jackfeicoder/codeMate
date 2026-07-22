package com.codemate.config;

public record AppConfig(
        String provider,
        String model,
        String baseUrl,
        String apiKey,
        String projectContext,
        int maxAgentSteps
) {
    public String maskedApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            return "(missing)";
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
