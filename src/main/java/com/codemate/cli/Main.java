package com.codemate.cli;

import com.codemate.config.AppConfig;
import com.codemate.config.EnvLoader;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        AppConfig config = EnvLoader.load(Path.of("."));
        new CliApplication(config).run(System.in, System.out);
    }
}
