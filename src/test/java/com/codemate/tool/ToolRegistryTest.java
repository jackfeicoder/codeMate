package com.codemate.tool;

import com.codemate.llm.ToolCall;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistryTest {
    @TempDir
    Path workspace;

    @Test
    void readsListsAndSearchesOnlyInsideWorkspace() throws Exception {
        Path source = Files.createDirectories(workspace.resolve("src"));
        Files.writeString(source.resolve("Demo.java"), "class Demo { String value = \"codeMate\"; }");
        ToolRegistry tools = new ToolRegistry(workspace);

        ToolExecutionResult list = tools.execute(new ToolCall("1", "list_dir", "{\"directory_path\":\"src\"}"));
        ToolExecutionResult read = tools.execute(new ToolCall("2", "read_file", "{\"file_path\":\"src/Demo.java\"}"));
        ToolExecutionResult search = tools.execute(new ToolCall("3", "grep_code", "{\"query\":\"codeMate\"}"));
        ToolExecutionResult escaped = tools.execute(new ToolCall("4", "read_file", "{\"path\":\"../outside.txt\"}"));

        assertTrue(list.content().contains("Demo.java"));
        assertTrue(read.content().contains("class Demo"));
        assertTrue(search.content().contains("Demo.java:1"));
        assertTrue(escaped.error());
        assertFalse(tools.definitions().isEmpty());
    }
}
