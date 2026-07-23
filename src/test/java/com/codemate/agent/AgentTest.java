package com.codemate.agent;

import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmException;
import com.codemate.llm.LlmMessage;
import com.codemate.llm.LlmResponse;
import com.codemate.llm.StreamListener;
import com.codemate.render.PlainRenderer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTest {
    @Test
    void streamsAssistantResponseAndStoresHistory() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        FakeLlmClient llmClient = new FakeLlmClient("Hello from Agent.");
        Agent agent = new Agent(
                llmClient,
                new PlainRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8)),
                "System rules"
        );

        agent.run("Say hello");

        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Hello from Agent."));
        assertEquals(3, agent.conversationHistory().size());
        assertEquals("system", agent.conversationHistory().get(0).role());
        assertEquals("user", agent.conversationHistory().get(1).role());
        assertEquals("assistant", agent.conversationHistory().get(2).role());
        assertEquals("Say hello", llmClient.lastMessages().get(1).content());
    }

    @Test
    void resetKeepsOnlySystemPrompt() {
        Agent agent = new Agent(
                new FakeLlmClient("ok"),
                new PlainRenderer(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8)),
                "System rules"
        );

        agent.run("task");
        agent.reset();

        assertEquals(1, agent.conversationHistory().size());
        assertEquals("system", agent.conversationHistory().get(0).role());
    }

    @Test
    void llmErrorIsRenderedAndAssistantMessageIsNotStored() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Agent agent = new Agent(
                new FailingLlmClient(),
                new PlainRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8)),
                "System rules"
        );

        agent.run("task");

        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Error: upstream unavailable"));
        assertEquals(2, agent.conversationHistory().size());
    }

    @Test
    void trimsOldestCompletedTurnsWhenContextBudgetIsExceeded() {
        Agent agent = new Agent(
                new FakeLlmClient("response-text"),
                new PlainRenderer(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8)),
                "system prompt",
                1_000
        );

        agent.run("a".repeat(600));
        agent.run("b".repeat(600));

        assertEquals(1, agent.contextStats().trimmedTurns());
        assertEquals(3, agent.conversationHistory().size());
        assertEquals("b".repeat(600), agent.conversationHistory().get(1).content());
    }

    private static final class FakeLlmClient implements LlmClient {
        private final String response;
        private List<LlmMessage> lastMessages = List.of();

        private FakeLlmClient(String response) {
            this.response = response;
        }

        @Override
        public LlmResponse complete(List<LlmMessage> messages) {
            return new LlmResponse(response);
        }

        @Override
        public LlmResponse stream(List<LlmMessage> messages, StreamListener listener) {
            lastMessages = new ArrayList<>(messages);
            listener.onContentDelta(response);
            return new LlmResponse(response, 2, 3);
        }

        private List<LlmMessage> lastMessages() {
            return lastMessages;
        }
    }

    private static final class FailingLlmClient implements LlmClient {
        @Override
        public LlmResponse complete(List<LlmMessage> messages) {
            throw new LlmException("upstream unavailable");
        }

        @Override
        public LlmResponse stream(List<LlmMessage> messages, StreamListener listener) {
            throw new LlmException("upstream unavailable");
        }
    }
}
