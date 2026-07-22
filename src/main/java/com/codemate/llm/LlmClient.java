package com.codemate.llm;

import java.util.List;

public interface LlmClient {
    LlmResponse complete(List<LlmMessage> messages);
}
