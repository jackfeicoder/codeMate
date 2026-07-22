package com.codemate.cli;

public record ParsedCommand(CommandType type, String payload) {
    public static ParsedCommand none() {
        return new ParsedCommand(CommandType.NONE, "");
    }
}
