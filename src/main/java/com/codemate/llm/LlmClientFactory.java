package com.codemate.llm;

import com.codemate.config.AppConfig;

public final class LlmClientFactory {
    private LlmClientFactory() {
    }

    public static LlmClient create(AppConfig config) {
        return new OpenAiCompatibleClient(config.baseUrl(), config.apiKey(), config.model());
    }
}
