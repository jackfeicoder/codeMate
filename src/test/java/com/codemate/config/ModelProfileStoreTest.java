package com.codemate.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelProfileStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsProfilesAndPersistsActiveSelection() throws IOException {
        Path configDir = tempDir.resolve(".codemate");
        Files.createDirectories(configDir);
        Files.writeString(configDir.resolve("models.properties"), """
                active=deepseek
                profile.deepseek.provider=deepseek
                profile.deepseek.model=deepseek-v4-flash
                profile.deepseek.base-url=https://api.deepseek.com
                profile.deepseek.api-key=key-one
                profile.ccswitch.provider=ccswitch
                profile.ccswitch.model=grok-4.5
                profile.ccswitch.base-url=http://127.0.0.1:3000/v1
                profile.ccswitch.api-key=key-two
                """);

        ModelProfileStore store = ModelProfileStore.load(tempDir, fallback());
        AppConfig selected = store.activate("ccswitch");

        assertEquals("grok-4.5", selected.model());
        assertEquals("ccswitch", store.activeProfileName());
        assertEquals(2, store.profiles().size());
        assertTrue(Files.readString(configDir.resolve("models.properties")).contains("active=ccswitch"));
    }

    @Test
    void fallsBackToDefaultProfileWhenNoLocalFileExists() {
        ModelProfileStore store = ModelProfileStore.load(tempDir, fallback());

        assertEquals("default", store.activeProfileName());
        assertEquals("fallback-model", store.activeConfig().model());
    }

    @Test
    void resolvesDefaultProfileKeyFromFallbackConfiguration() throws IOException {
        Path configDir = tempDir.resolve(".codemate");
        Files.createDirectories(configDir);
        Files.writeString(configDir.resolve("models.properties"), """
                active=deepseek
                profile.deepseek.provider=deepseek
                profile.deepseek.model=deepseek-v4-flash
                profile.deepseek.base-url=https://api.deepseek.com
                profile.deepseek.api-key=${CODEMATE_API_KEY}
                """);

        AppConfig fallback = new AppConfig(
                "fallback", "fallback-model", "https://example.com", "fallback-key", "CODEMATE.md", 8
        );
        ModelProfileStore store = ModelProfileStore.load(tempDir, fallback);

        assertEquals("fallback-key", store.activeConfig().apiKey());
    }

    @Test
    void createsTemplateOnlyOnce() throws IOException {
        ModelProfileStore store = ModelProfileStore.load(tempDir, fallback());

        Path created = store.initializeTemplate();

        assertTrue(Files.exists(created));
        assertTrue(Files.readString(created).contains("profile.ccswitch.model=grok-4.5"));
        assertThrows(IllegalStateException.class, store::initializeTemplate);
    }

    private static AppConfig fallback() {
        return new AppConfig("fallback", "fallback-model", "https://example.com", "", "CODEMATE.md", 8);
    }
}
