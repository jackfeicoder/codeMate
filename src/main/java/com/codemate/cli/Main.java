package com.codemate.cli;

import com.codemate.agent.Agent;
import com.codemate.agent.PromptLoader;
import com.codemate.config.AppConfig;
import com.codemate.config.EnvLoader;
import com.codemate.config.ModelProfileStore;
import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmClientFactory;
import com.codemate.render.PlainRenderer;
import com.codemate.render.Renderer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        AppConfig config = EnvLoader.load(Path.of("."));
        ModelProfileStore modelProfiles = ModelProfileStore.load(Path.of("."), config);
        config = modelProfiles.activeConfig();
        Renderer renderer = new PlainRenderer(System.out);
        LlmClient llmClient = LlmClientFactory.create(config);
        String systemPrompt = PromptLoader.loadSystemPrompt();
        Agent agent = new Agent(llmClient, renderer, systemPrompt);
        CliApplication application = new CliApplication(config, renderer, agent, modelProfiles, systemPrompt);
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
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
}
