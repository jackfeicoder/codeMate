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
    private final List<LlmMessage> conversationHistory = new ArrayList<>();

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.systemPrompt = requireText(systemPrompt, "systemPrompt");
        reset();
    }

    public void run(String userInput) {
        String trimmed = userInput == null ? "" : userInput.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        conversationHistory.add(LlmMessage.user(trimmed));
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
    }

    public List<LlmMessage> conversationHistory() {
        return List.copyOf(conversationHistory);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.strip();
    }
}
