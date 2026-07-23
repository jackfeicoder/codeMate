package com.codemate.tool;

import java.util.Map;
import java.util.Objects;

public record ToolDefinition(String name, String description, Map<String, Object> parameters) {
    public ToolDefinition {
        name = requireText(name, "name");
        description = requireText(description, "description");
        parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters"));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
