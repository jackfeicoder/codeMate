package com.codemate.cli;

import com.codemate.config.AppConfig;
import com.codemate.render.PlainRenderer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliApplicationTest {
    @Test
    void normalInputIsRecordedUntilAgentIsWired() {
        TestCli testCli = newTestCli();
        boolean shouldContinue = testCli.app().handleLine("summarize README");

        assertTrue(shouldContinue);
        assertEquals(1, testCli.app().submittedInputCount());
        assertTrue(testCli.output().contains("Agent runtime is not wired yet."));
    }

    @Test
    void clearResetsSessionState() {
        TestCli testCli = newTestCli();
        testCli.app().handleLine("first task");

        boolean shouldContinue = testCli.app().handleLine("/clear");

        assertTrue(shouldContinue);
        assertEquals(0, testCli.app().submittedInputCount());
        assertTrue(testCli.output().contains("Session state cleared."));
    }

    @Test
    void exitStopsLoop() {
        TestCli testCli = newTestCli();
        boolean shouldContinue = testCli.app().handleLine("/exit");

        assertFalse(shouldContinue);
        assertTrue(testCli.output().contains("Goodbye."));
    }

    @Test
    void unknownCommandContinuesLoop() {
        TestCli testCli = newTestCli();
        boolean shouldContinue = testCli.app().handleLine("/unknown");

        assertTrue(shouldContinue);
        assertTrue(testCli.output().contains("Unknown command: /unknown"));
    }

    private static TestCli newTestCli() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        CliApplication app = new CliApplication(
                new AppConfig("deepseek", "deepseek-chat", "https://example.com", "", "CODEMATE.md", 8),
                new PlainRenderer(output)
        );
        return new TestCli(app, bytes);
    }

    private record TestCli(CliApplication app, ByteArrayOutputStream bytes) {
        String output() {
            return bytes.toString(StandardCharsets.UTF_8);
        }
    }
}
