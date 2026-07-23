package com.codemate.cli;

import com.codemate.config.ModelProfile;
import com.codemate.config.ModelProfileStore;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Locale;

final class CodeMateCompleter implements Completer {
    private static final List<CommandOption> COMMANDS = List.of(
            new CommandOption("/help", "Show available commands"),
            new CommandOption("/model", "Show, list, or switch model profiles"),
            new CommandOption("/cd", "Show or switch the active workspace"),
            new CommandOption("/context", "Show current conversation context usage"),
            new CommandOption("/clear", "Clear current session state"),
            new CommandOption("/exit", "Exit codeMate"),
            new CommandOption("/quit", "Exit codeMate")
    );

    private final ModelProfileStore modelProfiles;

    CodeMateCompleter(ModelProfileStore modelProfiles) {
        this.modelProfiles = modelProfiles;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (line == null || candidates == null) {
            return;
        }
        String input = line.line() == null ? "" : line.line();
        if (!input.startsWith("/")) {
            return;
        }
        if (input.equalsIgnoreCase("/model") || input.regionMatches(true, 0, "/model ", 0, 7)) {
            completeModel(input, candidates);
            return;
        }
        completeCommands(line.word(), candidates);
    }

    private void completeCommands(String word, List<Candidate> candidates) {
        String prefix = word == null ? "" : word;
        for (CommandOption command : COMMANDS) {
            if (matches(command.value(), prefix)) {
                candidates.add(candidate(command.value(), "Command", command.description(), true));
            }
        }
    }

    private void completeModel(String input, List<Candidate> candidates) {
        if (input.equalsIgnoreCase("/model")) {
            for (ModelProfile profile : modelProfiles.profiles()) {
                String value = "/model " + profile.config().model();
                String description = profile.name() + " / " + profile.config().provider();
                candidates.add(candidate(value, "Model profile", description, true));
            }
            return;
        }

        String payload = input.length() == 6 ? "" : input.substring(7);
        String trimmed = payload.trim();
        if (trimmed.regionMatches(true, 0, "use ", 0, 4)) {
            completeProfiles(trimmed.substring(4).trim(), candidates);
            return;
        }

        addModelActions(trimmed, candidates);
        completeProfiles(trimmed, candidates);
    }

    private void addModelActions(String prefix, List<Candidate> candidates) {
        if (matches("list", prefix)) {
            candidates.add(candidate("list", "Model command", "List saved model profiles", true));
        }
        if (matches("init", prefix)) {
            candidates.add(candidate("init", "Model command", "Create the local profile template", true));
        }
        if (matches("use", prefix)) {
            candidates.add(candidate("use ", "Model command", "Switch to a named model profile", false));
        }
    }

    private void completeProfiles(String prefix, List<Candidate> candidates) {
        for (ModelProfile profile : modelProfiles.profiles()) {
            String model = profile.config().model();
            if (matches(model, prefix)) {
                String description = profile.name() + " / " + profile.config().provider();
                candidates.add(candidate(model, "Model profile", description, true));
            }
        }
    }

    private static Candidate candidate(String value, String group, String description, boolean complete) {
        return new Candidate(value, value.trim(), group, description, null, null, complete);
    }

    private static boolean matches(String value, String prefix) {
        return prefix == null || prefix.isBlank()
                || value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    private record CommandOption(String value, String description) {
    }
}
