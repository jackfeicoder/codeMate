package com.codemate.agent;

import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmException;
import com.codemate.llm.LlmMessage;
import com.codemate.llm.LlmResponse;
import com.codemate.render.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Agent {
    private final LlmClient llmClient;
    private final Renderer renderer;
    private final String systemPrompt;
    private final int maxContextCharacters;
    private final List<LlmMessage> conversationHistory = new ArrayList<>();
    private int trimmedTurns;

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt) {
        this(llmClient, renderer, systemPrompt, defaultMaxContextCharacters());
    }

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt, int maxContextCharacters) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.systemPrompt = requireText(systemPrompt, "systemPrompt");
        if (maxContextCharacters < 1_000) {
            throw new IllegalArgumentException("maxContextCharacters must be at least 1000");
        }
        this.maxContextCharacters = maxContextCharacters;
        reset();
    }

    public void run(String userInput) {
        String trimmed = userInput == null ? "" : userInput.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        conversationHistory.add(LlmMessage.user(trimmed));
        trimContextIfNeeded();
        try {
            LlmResponse response = llmClient.stream(conversationHistory, renderer::assistantDelta);
            renderer.assistantDone();
            conversationHistory.add(LlmMessage.assistant(response.content()));
        } catch (LlmException e) {
            renderer.error(e.getMessage());
        }
    }

    public void reset() {
        conversationHistory.clear();
        conversationHistory.add(LlmMessage.system(systemPrompt));
        trimmedTurns = 0;
    }

    public List<LlmMessage> conversationHistory() {
        return List.copyOf(conversationHistory);
    }

    public ContextStats contextStats() {
        return new ContextStats(
                conversationHistory.size(),
                conversationHistory.stream().mapToInt(message -> message.content().length()).sum(),
                maxContextCharacters,
                trimmedTurns
        );
    }

    private void trimContextIfNeeded() {
        while (contextStats().characters() > maxContextCharacters && conversationHistory.size() > 2) {
            conversationHistory.remove(1);
            if (conversationHistory.size() > 1 && "assistant".equals(conversationHistory.get(1).role())) {
                conversationHistory.remove(1);
            }
            trimmedTurns++;
        }
    }

    private static int defaultMaxContextCharacters() {
        String raw = System.getenv("CODEMATE_CONTEXT_MAX_CHARS");
        if (raw == null || raw.isBlank()) {
            return 24_000;
        }
        try {
            return Math.max(1_000, Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ignored) {
            return 24_000;
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.strip();
    }
}
