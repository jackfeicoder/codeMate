package com.codemate.cli;

import com.codemate.config.AppConfig;
import com.codemate.config.ModelProfileStore;
import org.jline.reader.Candidate;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeMateCompleterTest {
    @TempDir
    Path tempDir;

    @Test
    void completesSlashCommandsByPrefix() {
        List<String> values = complete("/m");

        assertTrue(values.contains("/model"));
        assertFalse(values.contains("/clear"));
    }

    @Test
    void completesDirectModelProfileByPrefix() {
        List<String> values = complete("/model d");

        assertTrue(values.contains("deepseek"));
        assertFalse(values.contains("ccswitch"));
    }

    @Test
    void completesModelProfileAfterUseSubcommand() {
        List<String> values = complete("/model use c");

        assertTrue(values.contains("ccswitch"));
        assertFalse(values.contains("deepseek"));
    }

    private List<String> complete(String input) {
        CodeMateCompleter completer = new CodeMateCompleter(profileStore());
        DefaultParser parser = new DefaultParser();
        List<Candidate> candidates = new ArrayList<>();
        completer.complete(null, parser.parse(input, input.length(), Parser.ParseContext.COMPLETE), candidates);
        return candidates.stream().map(Candidate::value).toList();
    }

    private ModelProfileStore profileStore() {
        try {
            Path configDir = tempDir.resolve(".codemate");
            Files.createDirectories(configDir);
            Files.writeString(configDir.resolve("models.properties"), """
                    active=deepseek
                    profile.deepseek.provider=deepseek
                    profile.deepseek.model=deepseek-v4-flash
                    profile.deepseek.base-url=https://api.deepseek.com
                    profile.ccswitch.provider=ccswitch
                    profile.ccswitch.model=grok-4.5
                    profile.ccswitch.base-url=http://127.0.0.1:3000/v1
                    """);
            return ModelProfileStore.load(tempDir,
                    new AppConfig("fallback", "fallback", "https://example.com", "", "CODEMATE.md", 8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
