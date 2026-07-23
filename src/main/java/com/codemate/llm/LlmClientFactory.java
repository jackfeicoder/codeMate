package com.codemate.llm;

import com.codemate.config.AppConfig;

public final class LlmClientFactory {
    private LlmClientFactory() {
    }

    public static LlmClient create(AppConfig config) {
        boolean forceHttp11 = "deepseek".equalsIgnoreCase(config.provider());
        return new OpenAiCompatibleClient(config.baseUrl(), config.apiKey(), config.model(), forceHttp11);
    }
}
