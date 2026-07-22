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
        new CliApplication(config, renderer, agent, modelProfiles, systemPrompt).run(System.in);
    }
}
