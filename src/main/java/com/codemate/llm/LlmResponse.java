package com.codemate.llm;

public record LlmResponse(String content) {
    public LlmResponse {
        if (content == null) {
            content = "";
        }
    }
}
