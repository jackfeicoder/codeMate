package com.codemate.config;

import java.util.Objects;

public record ModelProfile(String name, AppConfig config) {
    public ModelProfile {
        if (name == null || !name.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Model profile name must contain only letters, digits, '-' or '_'");
        }
        Objects.requireNonNull(config, "config");
    }
}
