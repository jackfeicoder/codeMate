package com.codemate.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppHomeResolverTest {
    @Test
    void usesFallbackDirectoryWhenNoHomePropertyIsConfigured() {
        Path fallback = Path.of("workspace").toAbsolutePath().normalize();

        assertEquals(fallback, AppHomeResolver.resolve(fallback, ""));
    }

    @Test
    void prefersConfiguredApplicationHome() {
        Path configured = Path.of("configured-home").toAbsolutePath().normalize();

        assertEquals(configured, AppHomeResolver.resolve(Path.of("workspace"), "configured-home"));
    }
}
