package com.codemate.tool;

import com.codemate.llm.ToolCall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Read-only tools exposed to the initial ReAct loop. */
public final class ToolRegistry {
    private static final int MAX_FILE_CHARACTERS = 60_000;
    private static final int MAX_DIRECTORY_ENTRIES = 200;
    private static final int MAX_SEARCH_MATCHES = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Path workspace;
    private final PathGuard pathGuard;
    private final List<ToolDefinition> definitions;

    public ToolRegistry(Path workspace) {
        this.workspace = workspace.toAbsolutePath().normalize();
        this.pathGuard = new PathGuard(this.workspace);
        this.definitions = List.of(
                definition("list_dir", "List files and directories under a path in the active workspace.",
                        Map.of("path", Map.of("type", "string", "description", "Relative directory path. Defaults to the workspace root."))),
                definition("read_file", "Read a UTF-8 text file from the active workspace.",
                        Map.of("path", Map.of("type", "string", "description", "Relative file path.")), List.of("path")),
                definition("grep_code", "Search UTF-8 text files for a literal query in the active workspace.",
                        Map.of(
                                "query", Map.of("type", "string", "description", "Literal text to find."),
                                "path", Map.of("type", "string", "description", "Relative file or directory path. Defaults to the workspace root.")
                        ), List.of("query"))
        );
    }

    public List<ToolDefinition> definitions() {
        return definitions;
    }

    public ToolExecutionResult execute(ToolCall call) {
        if (call == null || call.name().isBlank()) {
            return new ToolExecutionResult("unknown", "Tool call is missing a name.", true);
        }
        try {
            Map<String, Object> arguments = OBJECT_MAPPER.readValue(call.arguments(), new TypeReference<>() { });
            return switch (call.name()) {
                case "list_dir" -> new ToolExecutionResult(call.name(), listDirectory(stringValue(arguments, "path")), false);
                case "read_file" -> new ToolExecutionResult(call.name(), readFile(stringValue(arguments, "path")), false);
                case "grep_code" -> new ToolExecutionResult(call.name(), grepCode(
                        requiredString(arguments, "query"), stringValue(arguments, "path")), false);
                default -> new ToolExecutionResult(call.name(), "Unknown tool: " + call.name(), true);
            };
        } catch (Exception e) {
            return new ToolExecutionResult(call.name(), e.getMessage() == null ? "Tool execution failed" : e.getMessage(), true);
        }
    }

    private String listDirectory(String requestedPath) throws IOException {
        Path directory = pathGuard.resolveExisting(requestedPath);
        if (!Files.isDirectory(directory)) {
            throw new IOException("Not a directory: " + display(directory));
        }
        List<String> entries;
        try (Stream<Path> stream = Files.list(directory)) {
            entries = stream.sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .limit(MAX_DIRECTORY_ENTRIES)
                    .map(path -> (Files.isDirectory(path) ? "[dir] " : "[file] ") + display(path))
                    .toList();
        }
        String suffix = entries.size() == MAX_DIRECTORY_ENTRIES ? "\n... output truncated" : "";
        return String.join("\n", entries) + suffix;
    }

    private String readFile(String requestedPath) throws IOException {
        Path file = pathGuard.resolveExisting(requiredText(requestedPath, "path"));
        if (!Files.isRegularFile(file)) {
            throw new IOException("Not a regular file: " + display(file));
        }
        String content = Files.readString(file, StandardCharsets.UTF_8);
        if (content.length() > MAX_FILE_CHARACTERS) {
            return content.substring(0, MAX_FILE_CHARACTERS) + "\n... file output truncated";
        }
        return content;
    }

    private String grepCode(String query, String requestedPath) throws IOException {
        Path root = pathGuard.resolveExisting(requestedPath);
        List<String> matches = new ArrayList<>();
        try (Stream<Path> stream = Files.isDirectory(root) ? Files.walk(root, 20) : Stream.of(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                if (matches.size() >= MAX_SEARCH_MATCHES || Files.size(file) > 1_000_000) {
                    continue;
                }
                List<String> lines;
                try {
                    lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                } catch (IOException ignored) {
                    continue;
                }
                for (int index = 0; index < lines.size() && matches.size() < MAX_SEARCH_MATCHES; index++) {
                    if (lines.get(index).contains(query)) {
                        matches.add(display(file) + ":" + (index + 1) + ": " + lines.get(index).strip());
                    }
                }
            }
        }
        return matches.isEmpty() ? "No matches for: " + query : String.join("\n", matches);
    }

    private String display(Path path) {
        return workspace.relativize(path.toAbsolutePath().normalize()).toString();
    }

    private static ToolDefinition definition(String name, String description, Map<String, Object> properties) {
        return definition(name, description, properties, List.of());
    }

    private static ToolDefinition definition(String name, String description, Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        schema.put("additionalProperties", false);
        return new ToolDefinition(name, description, schema);
    }

    private static String requiredString(Map<String, Object> values, String key) {
        return requiredText(stringValue(values, key), key);
    }

    private static String stringValue(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : value.toString();
    }

    private static String requiredText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
