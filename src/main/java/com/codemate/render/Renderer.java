package com.codemate.render;

import com.codemate.config.AppConfig;

import java.io.PrintStream;

public interface Renderer {
    void startup(AppConfig config);

    void helpHint();

    void help();

    void prompt();

    void sessionCleared();

    void goodbye();

    void unknownCommand(String command);

    void agentNotReady(String input);

    void assistantDelta(String delta);

    void assistantDone();

    void error(String message);

    PrintStream stream();
}
