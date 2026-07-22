package com.codemate.cli;

import com.codemate.agent.Agent;
import com.codemate.agent.PromptLoader;
import com.codemate.config.AppConfig;
import com.codemate.config.EnvLoader;
import com.codemate.llm.LlmClient;
import com.codemate.llm.LlmClientFactory;
import com.codemate.render.PlainRenderer;
import com.codemate.render.Renderer;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        AppConfig config = EnvLoader.load(Path.of("."));
        Renderer renderer = new PlainRenderer(System.out);
        LlmClient llmClient = LlmClientFactory.create(config);
        Agent agent = new Agent(llmClient, renderer, PromptLoader.loadSystemPrompt());
        new CliApplication(config, renderer, agent).run(System.in);
    }
}
