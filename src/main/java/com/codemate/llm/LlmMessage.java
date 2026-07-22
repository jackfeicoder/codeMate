package com.codemate.llm;

import java.util.Objects;

public record LlmMessage(String role, String content) {
    public LlmMessage {
        role = requireText(role, "role");
        content = Objects.requireNonNull(content, "content");
    }

    public static LlmMessage system(String content) {
        return new LlmMessage("system", content);
    }

    public static LlmMessage user(String content) {
        return new LlmMessage("user", content);
    }

    public static LlmMessage assistant(String content) {
        return new LlmMessage("assistant", content);
    }

    public static LlmMessage tool(String content) {
        return new LlmMessage("tool", content);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
