package com.codemate.cli;

import com.codemate.agent.Agent;
import com.codemate.config.AppConfig;
import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmMessage;
import com.codemate.llm.LlmResponse;
import com.codemate.llm.StreamListener;
import com.codemate.render.PlainRenderer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliApplicationTest {
    @Test
    void normalInputIsSentToAgent() {
        TestCli testCli = newTestCli();
        boolean shouldContinue = testCli.app().handleLine("summarize README");

        assertTrue(shouldContinue);
        assertEquals(1, testCli.app().submittedInputCount());
        assertTrue(testCli.output().contains("agent response"));
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

    @Test
    void modelCommandListsSelectableProfiles() {
        TestCli testCli = newTestCli();

        boolean shouldContinue = testCli.app().handleLine("/model");

        assertTrue(shouldContinue);
        assertTrue(testCli.output().contains("Model profiles:"));
        assertTrue(testCli.output().contains("* default"));
    }

    private static TestCli newTestCli() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        PlainRenderer renderer = new PlainRenderer(output);
        Agent agent = new Agent(new FakeLlmClient(), renderer, "System rules");
        CliApplication app = new CliApplication(
                new AppConfig("deepseek", "deepseek-chat", "https://example.com", "", "CODEMATE.md", 8),
                renderer,
                agent
        );
        return new TestCli(app, bytes);
    }

    private record TestCli(CliApplication app, ByteArrayOutputStream bytes) {
        String output() {
            return bytes.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class FakeLlmClient implements LlmClient {
        @Override
        public LlmResponse complete(List<LlmMessage> messages) {
            return new LlmResponse("agent response");
        }

        @Override
        public LlmResponse stream(List<LlmMessage> messages, StreamListener listener) {
            listener.onContentDelta("agent response");
            return new LlmResponse("agent response");
        }
    }
}
