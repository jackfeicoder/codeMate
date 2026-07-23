package com.codemate.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class PathGuard {
    private final Path workspace;
    private final Path realWorkspace;

    PathGuard(Path workspace) {
        this.workspace = workspace.toAbsolutePath().normalize();
        try {
            this.realWorkspace = this.workspace.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Workspace is not accessible: " + this.workspace, e);
        }
    }

    Path resolveExisting(String value) throws IOException {
        Path candidate = resolve(value);
        if (!Files.exists(candidate)) {
            throw new IOException("Path does not exist: " + candidate);
        }
        Path realPath = candidate.toRealPath();
        if (!realPath.startsWith(realWorkspace)) {
            throw new IOException("Path escapes the active workspace");
        }
        return realPath;
    }

    Path resolve(String value) throws IOException {
        String text = value == null || value.isBlank() ? "." : value.trim();
        Path candidate = Path.of(text);
        if (!candidate.isAbsolute()) {
            candidate = workspace.resolve(candidate);
        }
        candidate = candidate.toAbsolutePath().normalize();
        if (!candidate.startsWith(workspace)) {
            throw new IOException("Path escapes the active workspace");
        }
        return candidate;
    }
}
