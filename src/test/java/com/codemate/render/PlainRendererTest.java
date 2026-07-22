package com.codemate.render;

import com.codemate.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlainRendererTest {
    @Test
    void startupPrintsMaskedConfiguration() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PlainRenderer renderer = new PlainRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        renderer.startup(new AppConfig("deepseek", "deepseek-chat", "https://example.com", "abcd12345678wxyz", "CODEMATE.md", 8));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Provider: deepseek"));
        assertTrue(output.contains("Model: deepseek-chat"));
        assertTrue(output.contains("API Key: abcd****wxyz"));
    }

    @Test
    void helpListsCoreCommands() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PlainRenderer renderer = new PlainRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        renderer.help();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("/help"));
        assertTrue(output.contains("/clear"));
        assertTrue(output.contains("/exit"));
        assertTrue(output.contains("/quit"));
    }

    @Test
    void assistantDeltaStreamsWithoutExtraFormatting() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PlainRenderer renderer = new PlainRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        renderer.assistantDelta("Hello");
        renderer.assistantDelta(" world");
        renderer.assistantDone();

        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Hello world"));
    }
}
