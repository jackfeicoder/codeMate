package com.codemate.render;

import com.codemate.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InlineRendererTest {
    @Test
    void startupUsesCodeMateChineseWelcomeScreenWithoutApiKey() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        InlineRenderer renderer = new InlineRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        renderer.startup(new AppConfig("deepseek", "deepseek-v4-flash", "https://example.com", "secret-key", "CODEMATE.md", 8));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("codeMate v0.1"));
        assertTrue(output.contains("欢迎回来！"));
        assertTrue(output.contains("快速开始"));
        assertTrue(output.contains("deepseek-v4-flash"));
        assertTrue(!output.contains("secret-key"));
    }

    @Test
    void usesCodeMateInputPromptAndGroupsStreamingAssistantOutput() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        InlineRenderer renderer = new InlineRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        renderer.assistantDelta("你好");
        renderer.assistantDelta("，我在。");
        renderer.assistantDone();

        assertEquals("\u001B[38;5;44m\u001B[1m* \u001B[0m", renderer.inputPrompt());
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("codeMate"));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("你好，我在。"));
    }
}
