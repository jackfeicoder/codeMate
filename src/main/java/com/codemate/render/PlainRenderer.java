package com.codemate.render;

import com.codemate.config.AppConfig;

import java.io.PrintStream;
import java.util.Objects;

public class PlainRenderer implements Renderer {
    private final PrintStream output;

    public PlainRenderer(PrintStream output) {
        this.output = Objects.requireNonNull(output, "output");
    }

    @Override
    public void startup(AppConfig config) {
        output.println("codeMate CLI");
        output.println("Provider: " + config.provider());
        output.println("Model: " + config.model());
        output.println("API Key: " + config.maskedApiKey());
        output.println("Project context: " + config.projectContext());
        output.println("Max agent steps: " + config.maxAgentSteps());
    }

    @Override
    public void helpHint() {
        output.println("Type /help for commands.");
    }

    @Override
    public void help() {
        output.println("Available commands:");
        output.println("  /help   Show commands");
        output.println("  /clear  Clear current session state");
        output.println("  /exit   Exit codeMate");
        output.println("  /quit   Exit codeMate");
    }

    @Override
    public void prompt() {
        output.print("> ");
        output.flush();
    }

    @Override
    public void sessionCleared() {
        output.println("Session state cleared.");
    }

    @Override
    public void goodbye() {
        output.println("Goodbye.");
    }

    @Override
    public void unknownCommand(String command) {
        output.println("Unknown command: " + command);
        output.println("Type /help to see available commands.");
    }

    @Override
    public void agentNotReady(String input) {
        output.println("Agent runtime is not wired yet.");
        output.println("Received: " + input);
    }

    @Override
    public void assistantDelta(String delta) {
        output.print(delta);
        output.flush();
    }

    @Override
    public void assistantDone() {
        output.println();
    }

    @Override
    public void error(String message) {
        output.println("Error: " + message);
    }

    @Override
    public PrintStream stream() {
        return output;
    }
}
