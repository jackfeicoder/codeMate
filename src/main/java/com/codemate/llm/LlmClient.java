package com.codemate.llm;

import com.codemate.tool.ToolDefinition;

import java.util.List;

public interface LlmClient {
    LlmResponse complete(List<LlmMessage> messages);

    LlmResponse stream(List<LlmMessage> messages, StreamListener listener);

    default LlmResponse stream(List<LlmMessage> messages, List<ToolDefinition> tools, StreamListener listener) {
        return stream(messages, listener);
    }
}
