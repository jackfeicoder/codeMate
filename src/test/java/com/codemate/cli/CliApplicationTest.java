package com.codemate.cli;

import com.codemate.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliApplicationTest {
    private final CliApplication app = new CliApplication(
            new AppConfig("deepseek", "deepseek-chat", "https://example.com", "", "CODEMATE.md", 8)
    );

    @Test
    void normalInputIsRecordedUntilAgentIsWired() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean shouldContinue = app.handleLine("summarize README", new PrintStream(bytes, true, StandardCharsets.UTF_8));

        assertTrue(shouldContinue);
        assertEquals(1, app.submittedInputCount());
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Agent runtime is not wired yet."));
    }

    @Test
    void clearResetsSessionState() {
        app.handleLine("first task", new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean shouldContinue = app.handleLine("/clear", new PrintStream(bytes, true, StandardCharsets.UTF_8));

        assertTrue(shouldContinue);
        assertEquals(0, app.submittedInputCount());
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Session state cleared."));
    }

    @Test
    void exitStopsLoop() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean shouldContinue = app.handleLine("/exit", new PrintStream(bytes, true, StandardCharsets.UTF_8));

        assertFalse(shouldContinue);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Goodbye."));
    }

    @Test
    void unknownCommandContinuesLoop() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean shouldContinue = app.handleLine("/unknown", new PrintStream(bytes, true, StandardCharsets.UTF_8));

        assertTrue(shouldContinue);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Unknown command: /unknown"));
    }
}
