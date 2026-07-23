package com.codemate.config;

import java.nio.file.Path;

public final class AppHomeResolver {
    public static final String HOME_PROPERTY = "codemate.home";

    private AppHomeResolver() {
    }

    public static Path resolve(Path fallbackDirectory) {
        return resolve(fallbackDirectory, System.getProperty(HOME_PROPERTY));
    }

    static Path resolve(Path fallbackDirectory, String configuredHome) {
        if (fallbackDirectory == null) {
            throw new IllegalArgumentException("fallbackDirectory must not be null");
        }
        Path home = configuredHome == null || configuredHome.isBlank()
                ? fallbackDirectory
                : Path.of(configuredHome.trim());
        return home.toAbsolutePath().normalize();
    }
}
