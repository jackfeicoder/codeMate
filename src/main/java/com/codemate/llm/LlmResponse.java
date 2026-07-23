package com.codemate.llm;

import java.util.List;

public record LlmResponse(String content, int inputTokens, int outputTokens, List<ToolCall> toolCalls) {
    public LlmResponse(String content) {
        this(content, 0, 0, List.of());
    }

    public LlmResponse(String content, int inputTokens, int outputTokens) {
        this(content, inputTokens, outputTokens, List.of());
    }

    public LlmResponse {
        if (content == null) {
            content = "";
        }
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }
}
