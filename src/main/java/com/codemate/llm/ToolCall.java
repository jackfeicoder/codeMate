package com.codemate.llm;

import java.util.Objects;

public record ToolCall(String id, String name, String arguments) {
    public ToolCall {
        id = Objects.requireNonNullElse(id, "");
        name = Objects.requireNonNullElse(name, "");
        arguments = Objects.requireNonNullElse(arguments, "{}");
    }
}
