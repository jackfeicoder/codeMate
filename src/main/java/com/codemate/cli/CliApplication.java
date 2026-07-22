package com.codemate.cli;

import com.codemate.config.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CliApplication {
    private final AppConfig config;
    private final List<String> submittedInputs = new ArrayList<>();

    public CliApplication(AppConfig config) {
        this.config = config;
    }

    public void run(InputStream input, PrintStream output) {
        printStartup(output);
        printHelpHint(output);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            while (true) {
                output.print("> ");
                output.flush();

                String line = reader.readLine();
                if (line == null) {
                    output.println();
                    output.println("Goodbye.");
                    return;
                }

                if (!handleLine(line, output)) {
                    return;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read CLI input", e);
        }
    }

    boolean handleLine(String input, PrintStream output) {
        ParsedCommand command = CliCommandParser.parse(input);
        return switch (command.type()) {
            case NONE -> handleNormalInput(input, output);
            case HELP -> {
                printHelp(output);
                yield true;
            }
            case CLEAR -> {
                submittedInputs.clear();
                output.println("Session state cleared.");
                yield true;
            }
            case EXIT -> {
                output.println("Goodbye.");
                yield false;
            }
            case UNKNOWN -> {
                output.println("Unknown command: " + command.payload());
                output.println("Type /help to see available commands.");
                yield true;
            }
        };
    }

    int submittedInputCount() {
        return submittedInputs.size();
    }

    private boolean handleNormalInput(String input, PrintStream output) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        submittedInputs.add(trimmed);
        output.println("Agent runtime is not wired yet.");
        output.println("Received: " + trimmed);
        return true;
    }

    private void printStartup(PrintStream output) {
        output.println("codeMate CLI");
        output.println("Provider: " + config.provider());
        output.println("Model: " + config.model());
        output.println("API Key: " + config.maskedApiKey());
        output.println("Project context: " + config.projectContext());
        output.println("Max agent steps: " + config.maxAgentSteps());
    }

    private void printHelpHint(PrintStream output) {
        output.println("Type /help for commands.");
    }

    private void printHelp(PrintStream output) {
        output.println("Available commands:");
        output.println("  /help   Show commands");
        output.println("  /clear  Clear current session state");
        output.println("  /exit   Exit codeMate");
        output.println("  /quit   Exit codeMate");
    }
}
