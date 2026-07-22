package com.codemate.cli;

import com.codemate.agent.Agent;
import com.codemate.config.AppConfig;
import com.codemate.config.ModelProfileStore;
import com.codemate.llm.LlmClientFactory;
import com.codemate.render.Renderer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CliApplication {
    private AppConfig config;
    private final Renderer renderer;
    private Agent agent;
    private final ModelProfileStore modelProfiles;
    private final String systemPrompt;
    private final List<String> submittedInputs = new ArrayList<>();

    public CliApplication(AppConfig config, Renderer renderer, Agent agent) {
        this(config, renderer, agent, ModelProfileStore.inMemory(config), "You are codeMate.");
    }

    public CliApplication(
            AppConfig config,
            Renderer renderer,
            Agent agent,
            ModelProfileStore modelProfiles,
            String systemPrompt
    ) {
        this.config = config;
        this.renderer = renderer;
        this.agent = agent;
        this.modelProfiles = modelProfiles;
        this.systemPrompt = systemPrompt;
    }

    public void run(InputStream input) {
        showStartup();

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

    public void run(LineReader lineReader) {
        showStartup();

        while (true) {
            try {
                String line = lineReader.readLine("> ");
                if (!handleLine(line)) {
                    return;
                }
            } catch (UserInterruptException e) {
                renderer.stream().println();
            } catch (EndOfFileException e) {
                renderer.stream().println();
                renderer.goodbye();
                return;
            }
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
            case MODEL -> {
                handleModel(command.payload());
                yield true;
            }
            case CLEAR -> {
                submittedInputs.clear();
                agent.reset();
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

    private void handleModel(String payload) {
        String argument = payload == null ? "" : payload.trim();
        if (argument.isEmpty()) {
            renderer.modelStatus(modelProfiles.activeProfileName(), config);
            return;
        }
        if (argument.equalsIgnoreCase("list")) {
            renderer.modelProfiles(modelProfiles.activeProfileName(), modelProfiles.profiles());
            return;
        }
        if (argument.equalsIgnoreCase("init")) {
            try {
                renderer.modelConfigPath(modelProfiles.initializeTemplate());
            } catch (IllegalStateException e) {
                renderer.error(e.getMessage());
            }
            return;
        }

        String profileName = argument.regionMatches(true, 0, "use ", 0, 4)
                ? argument.substring(4).trim()
                : argument;
        if (profileName.isEmpty()) {
            renderer.error("Usage: /model use <name>");
            return;
        }

        try {
            config = modelProfiles.activate(profileName);
            agent = new Agent(LlmClientFactory.create(config), renderer, systemPrompt);
            submittedInputs.clear();
            renderer.modelSwitched(modelProfiles.activeProfileName(), config);
        } catch (IllegalArgumentException | IllegalStateException e) {
            renderer.error(e.getMessage());
        }
    }

    int submittedInputCount() {
        return submittedInputs.size();
    }

    private void showStartup() {
        renderer.startup(config);
        renderer.helpHint();
    }

    private boolean handleNormalInput(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        submittedInputs.add(trimmed);
        agent.run(trimmed);
        return true;
    }
}
