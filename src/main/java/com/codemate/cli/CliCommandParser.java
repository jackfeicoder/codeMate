package com.codemate.cli;

public final class CliCommandParser {
    private CliCommandParser() {
    }

    public static ParsedCommand parse(String input) {
        if (input == null) {
            return ParsedCommand.none();
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return ParsedCommand.none();
        }

        if (equalsAny(trimmed, "/help", "help")) {
            return new ParsedCommand(CommandType.HELP, "");
        }

        if (trimmed.equalsIgnoreCase("/model")) {
            return new ParsedCommand(CommandType.MODEL, "");
        }

        if (trimmed.regionMatches(true, 0, "/model ", 0, 7)) {
            return new ParsedCommand(CommandType.MODEL, trimmed.substring(7).trim());
        }

        if (equalsAny(trimmed, "/clear", "clear")) {
            return new ParsedCommand(CommandType.CLEAR, "");
        }

        if (equalsAny(trimmed, "/exit", "/quit", "exit", "quit")) {
            return new ParsedCommand(CommandType.EXIT, "");
        }

        if (trimmed.startsWith("/")) {
            return new ParsedCommand(CommandType.UNKNOWN, trimmed);
        }

        return ParsedCommand.none();
    }

    private static boolean equalsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }
}
