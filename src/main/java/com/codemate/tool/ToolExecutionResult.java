package com.codemate.tool;

public record ToolExecutionResult(String name, String content, boolean error) {
    public ToolExecutionResult {
        content = content == null ? "" : content;
    }
}
