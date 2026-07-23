package com.codemate.render;

import com.codemate.agent.ContextStats;
import com.codemate.config.AppConfig;
import com.codemate.config.ModelProfile;

import java.nio.file.Path;
import java.util.List;

import java.io.PrintStream;

public interface Renderer {
    void startup(AppConfig config);

    void helpHint();

    void help();

    void modelStatus(String activeProfile, AppConfig config);

    void modelProfiles(String activeProfile, List<ModelProfile> profiles);

    void modelSwitched(String profileName, AppConfig config);

    void modelConfigPath(Path path);

    void workspaceChanged(Path workspace);

    void contextStatus(ContextStats stats);

    void prompt();

    String inputPrompt();

    void sessionCleared();

    void goodbye();

    void unknownCommand(String command);

    void agentNotReady(String input);

    void toolExecuting(String name, String arguments);

    void toolCompleted(String name, boolean error);

    void assistantDelta(String delta);

    void assistantDone();

    void error(String message);

    PrintStream stream();
}
