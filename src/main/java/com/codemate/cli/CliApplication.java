package com.codemate.cli;

import com.codemate.config.AppConfig;
import com.codemate.render.Renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CliApplication {
    private final AppConfig config;
    private final Renderer renderer;
    private final List<String> submittedInputs = new ArrayList<>();

    public CliApplication(AppConfig config, Renderer renderer) {
        this.config = config;
        this.renderer = renderer;
    }

    public void run(InputStream input) {
        renderer.startup(config);
        renderer.helpHint();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            while (true) {
                renderer.prompt();

                String line = reader.readLine();
                if (line == null) {
                    renderer.stream().println();
                    renderer.goodbye();
                    return;
                }

                if (!handleLine(line)) {
                    return;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read CLI input", e);
        }
    }

    boolean handleLine(String input) {
        ParsedCommand command = CliCommandParser.parse(input);
        return switch (command.type()) {
            case NONE -> handleNormalInput(input);
            case HELP -> {
                renderer.help();
                yield true;
            }
            case CLEAR -> {
                submittedInputs.clear();
                renderer.sessionCleared();
                yield true;
            }
            case EXIT -> {
                renderer.goodbye();
                yield false;
            }
            case UNKNOWN -> {
                renderer.unknownCommand(command.payload());
                yield true;
            }
        };
    }

    int submittedInputCount() {
        return submittedInputs.size();
    }

    private boolean handleNormalInput(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        submittedInputs.add(trimmed);
        renderer.agentNotReady(trimmed);
        return true;
    }
}
