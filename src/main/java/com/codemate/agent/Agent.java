package com.codemate.agent;

import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmException;
import com.codemate.llm.LlmMessage;
import com.codemate.llm.LlmResponse;
import com.codemate.llm.ToolCall;
import com.codemate.render.Renderer;
import com.codemate.tool.ToolExecutionResult;
import com.codemate.tool.ToolRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Agent {
    private final LlmClient llmClient;
    private final Renderer renderer;
    private final String systemPrompt;
    private final int maxContextCharacters;
    private final int maxAgentSteps;
    private final ToolRegistry toolRegistry;
    private final List<LlmMessage> conversationHistory = new ArrayList<>();
    private int trimmedTurns;

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt) {
        this(llmClient, renderer, systemPrompt, new ToolRegistry(java.nio.file.Path.of(".")), defaultMaxContextCharacters());
    }

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt, int maxContextCharacters) {
        this(llmClient, renderer, systemPrompt, new ToolRegistry(java.nio.file.Path.of(".")), maxContextCharacters);
    }

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt, ToolRegistry toolRegistry) {
        this(llmClient, renderer, systemPrompt, toolRegistry, defaultMaxContextCharacters(), 8);
    }

    public Agent(LlmClient llmClient, Renderer renderer, String systemPrompt, ToolRegistry toolRegistry, int maxContextCharacters) {
        this(llmClient, renderer, systemPrompt, toolRegistry, maxContextCharacters, 8);
    }

    public Agent(
            LlmClient llmClient,
            Renderer renderer,
            String systemPrompt,
            ToolRegistry toolRegistry,
            int maxContextCharacters,
            int maxAgentSteps
    ) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.systemPrompt = requireText(systemPrompt, "systemPrompt");
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry");
        if (maxContextCharacters < 1_000) {
            throw new IllegalArgumentException("maxContextCharacters must be at least 1000");
        }
        if (maxAgentSteps < 1) {
            throw new IllegalArgumentException("maxAgentSteps must be positive");
        }
        this.maxContextCharacters = maxContextCharacters;
        this.maxAgentSteps = maxAgentSteps;
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
            for (int step = 0; step < maxAgentSteps; step++) {
                LlmResponse response = llmClient.stream(conversationHistory, toolRegistry.definitions(), renderer::assistantDelta);
                if (response.toolCalls().isEmpty()) {
                    renderer.assistantDone();
                    conversationHistory.add(LlmMessage.assistant(response.content()));
                    return;
                }

                conversationHistory.add(LlmMessage.assistant(assistantToolCallSummary(response.toolCalls())));
                for (ToolCall toolCall : response.toolCalls()) {
                    renderer.toolExecuting(toolCall.name(), toolCall.arguments());
                    ToolExecutionResult result = toolRegistry.execute(toolCall);
                    renderer.toolCompleted(toolCall.name(), result.error());
                    conversationHistory.add(LlmMessage.user(toolResultMessage(toolCall, result)));
                }
                trimContextIfNeeded();
            }
            renderer.error("Agent reached the maximum ReAct tool steps (" + maxAgentSteps + ").");
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

    private static String assistantToolCallSummary(List<ToolCall> toolCalls) {
        return "<tool_calls>" + toolCalls.stream()
                .map(call -> call.name() + " " + call.arguments())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("") + "</tool_calls>";
    }

    private static String toolResultMessage(ToolCall toolCall, ToolExecutionResult result) {
        return "<tool_result name=\"" + toolCall.name() + "\" status=\""
                + (result.error() ? "error" : "ok") + "\">\n" + result.content() + "\n</tool_result>";
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

    public static int defaultMaxContextCharacters() {
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
