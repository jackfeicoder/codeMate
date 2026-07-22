package com.codemate.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void usesDefaultsWhenDotEnvIsMissing() {
        AppConfig config = EnvLoader.load(tempDir);

        assertEquals("deepseek", config.provider());
        assertEquals("deepseek-chat", config.model());
        assertEquals("https://api.deepseek.com/v1", config.baseUrl());
        assertEquals("", config.apiKey());
        assertEquals("CODEMATE.md", config.projectContext());
        assertEquals(8, config.maxAgentSteps());
    }

    @Test
    void readsDotEnvValues() throws IOException {
        Files.writeString(tempDir.resolve(".env"), """
                CODEMATE_PROVIDER=kimi
                CODEMATE_MODEL=kimi-k2
                CODEMATE_BASE_URL=https://api.moonshot.cn/v1
                CODEMATE_API_KEY=abcd12345678wxyz
                CODEMATE_PROJECT_CONTEXT=PROJECT.md
                CODEMATE_MAX_AGENT_STEPS=12
                """);

        AppConfig config = EnvLoader.load(tempDir);

        assertEquals("kimi", config.provider());
        assertEquals("kimi-k2", config.model());
        assertEquals("https://api.moonshot.cn/v1", config.baseUrl());
        assertEquals("abcd12345678wxyz", config.apiKey());
        assertEquals("PROJECT.md", config.projectContext());
        assertEquals(12, config.maxAgentSteps());
    }

    @Test
    void stripsQuotesAndIgnoresComments() throws IOException {
        Files.writeString(tempDir.resolve(".env"), """
                # codeMate local config
                CODEMATE_PROVIDER="deepseek"
                CODEMATE_MODEL='deepseek-reasoner'
                INVALID_LINE
                """);

        AppConfig config = EnvLoader.load(tempDir);

        assertEquals("deepseek", config.provider());
        assertEquals("deepseek-reasoner", config.model());
    }

    @Test
    void invalidMaxAgentStepsFallsBackToDefault() throws IOException {
        Files.writeString(tempDir.resolve(".env"), "CODEMATE_MAX_AGENT_STEPS=not-a-number");

        AppConfig config = EnvLoader.load(tempDir);

        assertEquals(8, config.maxAgentSteps());
    }
}
