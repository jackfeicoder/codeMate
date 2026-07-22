package com.codemate.llm;

public record LlmResponse(String content, int inputTokens, int outputTokens) {
    public LlmResponse(String content) {
        this(content, 0, 0);
    }

    public LlmResponse {
        if (content == null) {
            content = "";
        }
    }
}
