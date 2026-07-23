package com.codemate.config;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public final class ModelProfileStore {
    private static final String DEFAULT_PROFILE = "default";
    private static final String ACTIVE_KEY = "active";
    private static final String PROFILE_PREFIX = "profile.";
    private static final String PROVIDER_SUFFIX = ".provider";
    private static final String MODEL_SUFFIX = ".model";
    private static final String BASE_URL_SUFFIX = ".base-url";
    private static final String API_KEY_SUFFIX = ".api-key";

    private final Path path;
    private final AppConfig fallback;
    private final Map<String, ModelProfile> profiles;
    private final Properties properties;
    private String activeProfileName;

    private ModelProfileStore(
            Path path,
            AppConfig fallback,
            Map<String, ModelProfile> profiles,
            Properties properties,
            String activeProfileName
    ) {
        this.path = path;
        this.fallback = fallback;
        this.profiles = profiles;
        this.properties = properties;
        this.activeProfileName = activeProfileName;
    }

    public static ModelProfileStore load(Path projectRoot, AppConfig fallback) {
        Path path = projectRoot.resolve(".codemate").resolve("models.properties");
        Properties properties = readProperties(path);
        Map<String, ModelProfile> profiles = readProfiles(properties, fallback);
        if (profiles.isEmpty()) {
            profiles.put(DEFAULT_PROFILE, new ModelProfile(DEFAULT_PROFILE, fallback));
        }
        String active = properties.getProperty(ACTIVE_KEY, DEFAULT_PROFILE).trim();
        if (!profiles.containsKey(active)) {
            active = profiles.keySet().iterator().next();
        }
        return new ModelProfileStore(path, fallback, profiles, properties, active);
    }

    public static ModelProfileStore inMemory(AppConfig fallback) {
        Map<String, ModelProfile> profiles = new LinkedHashMap<>();
        profiles.put(DEFAULT_PROFILE, new ModelProfile(DEFAULT_PROFILE, fallback));
        return new ModelProfileStore(null, fallback, profiles, new Properties(), DEFAULT_PROFILE);
    }

    public AppConfig activeConfig() {
        return profiles.get(activeProfileName).config();
    }

    public String activeProfileName() {
        return activeProfileName;
    }

    public List<ModelProfile> profiles() {
        return List.copyOf(profiles.values());
    }

    public AppConfig activate(String requestedName) {
        ModelProfile profile = findProfile(requestedName);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown model or profile: " + requestedName + ". Run /model list first.");
        }
        activeProfileName = profile.name();
        persistActiveProfile();
        return profile.config();
    }

    private ModelProfile findProfile(String requestedName) {
        ModelProfile namedProfile = profiles.get(requestedName);
        if (namedProfile != null) {
            return namedProfile;
        }

        List<ModelProfile> matchingModels = profiles.values().stream()
                .filter(profile -> profile.config().model().equalsIgnoreCase(requestedName))
                .toList();
        if (matchingModels.size() > 1) {
            throw new IllegalArgumentException(
                    "Model name matches multiple profiles: " + requestedName + ". Use the profile name instead."
            );
        }
        return matchingModels.isEmpty() ? null : matchingModels.get(0);
    }

    public Path initializeTemplate() {
        if (path == null) {
            throw new IllegalStateException("Model profiles are not available in this runtime.");
        }
        if (Files.exists(path)) {
            throw new IllegalStateException("Model profile file already exists: " + path);
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, template(), StandardCharsets.UTF_8);
            return path;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create model profile file: " + path, e);
        }
    }

    private void persistActiveProfile() {
        if (path == null || !Files.exists(path)) {
            return;
        }
        properties.setProperty(ACTIVE_KEY, activeProfileName);
        try (StringWriter writer = new StringWriter()) {
            properties.store(writer, "codeMate local model profiles - never commit real keys");
            Files.writeString(path, writer.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save active model profile: " + path, e);
        }
    }

    private static Properties readProperties(Path path) {
        Properties properties = new Properties();
        if (!Files.exists(path)) {
            return properties;
        }
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read model profiles: " + path, e);
        }
    }

    private static Map<String, ModelProfile> readProfiles(Properties properties, AppConfig fallback) {
        Set<String> names = new TreeSet<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(PROFILE_PREFIX) && key.endsWith(PROVIDER_SUFFIX)) {
                names.add(key.substring(PROFILE_PREFIX.length(), key.length() - PROVIDER_SUFFIX.length()));
            }
        }

        Map<String, ModelProfile> profiles = new LinkedHashMap<>();
        for (String name : names) {
            String prefix = PROFILE_PREFIX + name;
            String provider = properties.getProperty(prefix + PROVIDER_SUFFIX, "").trim();
            String model = properties.getProperty(prefix + MODEL_SUFFIX, "").trim();
            String baseUrl = properties.getProperty(prefix + BASE_URL_SUFFIX, "").trim();
            String apiKey = resolveApiKey(properties.getProperty(prefix + API_KEY_SUFFIX, "").trim(), fallback);
            if (provider.isEmpty() || model.isEmpty() || baseUrl.isEmpty()) {
                continue;
            }
            profiles.put(name, new ModelProfile(name, new AppConfig(
                    provider, model, baseUrl, apiKey, fallback.projectContext(), fallback.maxAgentSteps()
            )));
        }
        return profiles;
    }

    private static String template() {
        return """
                # Local-only model profiles. This file is ignored by Git.
                active=deepseek

                profile.deepseek.provider=deepseek
                profile.deepseek.model=deepseek-v4-flash
                profile.deepseek.base-url=https://api.deepseek.com
                # Reuses CODEMATE_API_KEY from .env for the default profile.
                profile.deepseek.api-key=${CODEMATE_API_KEY}

                profile.ccswitch.provider=ccswitch
                profile.ccswitch.model=grok-4.5
                profile.ccswitch.base-url=http://127.0.0.1:3000/v1
                profile.ccswitch.api-key=your-ccswitch-key
                """;
    }

    private static String resolveApiKey(String apiKey, AppConfig fallback) {
        return "${CODEMATE_API_KEY}".equals(apiKey) ? fallback.apiKey() : apiKey;
    }
}
