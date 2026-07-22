package com.codemate.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {
    @Test
    void masksMissingApiKey() {
        AppConfig config = new AppConfig("deepseek", "deepseek-chat", "https://example.com", "", "CODEMATE.md", 8);

        assertEquals("(missing)", config.maskedApiKey());
    }

    @Test
    void masksShortApiKey() {
        AppConfig config = new AppConfig("deepseek", "deepseek-chat", "https://example.com", "short", "CODEMATE.md", 8);

        assertEquals("****", config.maskedApiKey());
    }

    @Test
    void masksLongApiKey() {
        AppConfig config = new AppConfig("deepseek", "deepseek-chat", "https://example.com", "abcd12345678wxyz", "CODEMATE.md", 8);

        assertEquals("abcd****wxyz", config.maskedApiKey());
    }
}
