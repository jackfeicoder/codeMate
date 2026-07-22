package com.codemate.agent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class PromptLoader {
    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are codeMate, a Java AI Agent CLI built for software engineering workflows.

            Answer clearly and keep responses focused on the user's software engineering task.
            """;

    private PromptLoader() {
    }

    public static String loadSystemPrompt() {
        String resourcePath = "prompts/react-system.md";
        ClassLoader classLoader = PromptLoader.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return DEFAULT_SYSTEM_PROMPT.strip();
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            return DEFAULT_SYSTEM_PROMPT.strip();
        }
    }
}
