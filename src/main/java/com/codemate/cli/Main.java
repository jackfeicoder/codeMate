package com.codemate.cli;

import com.codemate.agent.Agent;
import com.codemate.agent.PromptLoader;
import com.codemate.config.AppConfig;
import com.codemate.config.AppHomeResolver;
import com.codemate.config.EnvLoader;
import com.codemate.config.ModelProfileStore;
import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmClientFactory;
import com.codemate.render.InlineRenderer;
import com.codemate.render.PlainRenderer;
import com.codemate.render.Renderer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path applicationHome = AppHomeResolver.resolve(Path.of("."));
        AppConfig config = EnvLoader.load(applicationHome);
        ModelProfileStore modelProfiles = ModelProfileStore.load(applicationHome, config);
        config = modelProfiles.activeConfig();
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            Renderer renderer = createRenderer();
            LlmClient llmClient = LlmClientFactory.create(config);
            String systemPrompt = PromptLoader.loadSystemPrompt();
            String initialPrompt = systemPrompt + "\n\nCurrent workspace: "
                    + Path.of(".").toAbsolutePath().normalize();
            Agent agent = new Agent(llmClient, renderer, initialPrompt);
            CliApplication application = new CliApplication(config, renderer, agent, modelProfiles, systemPrompt);
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new DefaultParser().escapeChars(null))
                    .completer(new CodeMateCompleter(modelProfiles))
                    .build();
            lineReader.option(LineReader.Option.AUTO_LIST, true);
            lineReader.option(LineReader.Option.AUTO_MENU, true);
            lineReader.option(LineReader.Option.BRACKETED_PASTE, true);
            application.run(lineReader);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize interactive terminal", e);
        }
    }

    private static Renderer createRenderer() {
        if ("plain".equalsIgnoreCase(System.getenv("CODEMATE_RENDERER"))) {
            return new PlainRenderer(System.out);
        }
        return new InlineRenderer(System.out);
    }
}
