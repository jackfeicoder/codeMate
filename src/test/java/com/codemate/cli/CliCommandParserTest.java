package com.codemate.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CliCommandParserTest {
    @Test
    void emptyInputReturnsNone() {
        assertEquals(CommandType.NONE, CliCommandParser.parse("").type());
        assertEquals(CommandType.NONE, CliCommandParser.parse("   ").type());
        assertEquals(CommandType.NONE, CliCommandParser.parse(null).type());
    }

    @Test
    void parsesHelpCommand() {
        assertEquals(CommandType.HELP, CliCommandParser.parse("/help").type());
        assertEquals(CommandType.HELP, CliCommandParser.parse(" HELP ").type());
    }

    @Test
    void parsesClearCommand() {
        assertEquals(CommandType.CLEAR, CliCommandParser.parse("/clear").type());
        assertEquals(CommandType.CLEAR, CliCommandParser.parse("clear").type());
    }

    @Test
    void parsesExitCommand() {
        assertEquals(CommandType.EXIT, CliCommandParser.parse("/exit").type());
        assertEquals(CommandType.EXIT, CliCommandParser.parse("/quit").type());
        assertEquals(CommandType.EXIT, CliCommandParser.parse("exit").type());
        assertEquals(CommandType.EXIT, CliCommandParser.parse("quit").type());
    }

    @Test
    void parsesModelCommandAndPayload() {
        ParsedCommand command = CliCommandParser.parse("/model use deepseek");

        assertEquals(CommandType.MODEL, command.type());
        assertEquals("use deepseek", command.payload());
        assertEquals(CommandType.MODEL, CliCommandParser.parse("/model").type());
    }

    @Test
    void slashCommandUnknownWhenNotRegistered() {
        ParsedCommand command = CliCommandParser.parse("/unknown");

        assertEquals(CommandType.UNKNOWN, command.type());
        assertEquals("/unknown", command.payload());
    }

    @Test
    void normalTaskReturnsNone() {
        assertEquals(CommandType.NONE, CliCommandParser.parse("summarize README").type());
    }
}
