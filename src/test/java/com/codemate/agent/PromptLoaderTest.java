package com.codemate.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptLoaderTest {
    @Test
    void loadsSystemPromptFromResources() {
        String prompt = PromptLoader.loadSystemPrompt();

        assertTrue(prompt.contains("codeMate"));
        assertTrue(prompt.contains("Java AI Agent CLI"));
    }
}
