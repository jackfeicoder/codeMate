# codeMate

codeMate is a Java-based AI Agent CLI for software engineering workflows. It provides a terminal-first interface for model-driven task execution, project-aware context assembly, tool invocation, and controlled automation.

The project is designed as a maintainable command-line product rather than a simple chat wrapper. Its architecture separates CLI orchestration, model access, tool execution, safety policy, and rendering so each capability can evolve independently.

## Features

- ReAct-style agent loop
- Multi-provider model abstraction
- File reading, writing, directory listing, code search, and command execution
- Tool Registry for model-callable capabilities
- Path guard, command guard, human approval, and audit logging
- Plan-and-Execute task orchestration
- Project context injection
- JLine-based terminal interaction
- Slash commands, input history, and tool-call rendering

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 17 |
| Build | Maven |
| HTTP | OkHttp |
| JSON | Jackson |
| Terminal | JLine |
| Test | JUnit 5 |
| Optional extensions | SQLite, JavaParser, JGit, MCP, RAG |

## Status

Current implementation:

- Maven project skeleton
- Core package layout
- Minimal CLI entry point
- Environment-based runtime configuration
- Masked credential display
- Unit tests for configuration loading

## Quick Start

```bash
copy .env.example .env
mvn test
mvn package
java -jar target/codemate-0.1.0-SNAPSHOT.jar
```

The runtime reads configuration from `.env` and system environment variables. Sensitive values must stay local and must never be committed.

## Project Structure

```text
codeMate/
|-- docs/
|   |-- architecture.md       # Architecture, module responsibilities, data flow
|   `-- development-plan.md   # Product roadmap and delivery phases
|-- src/main/java/com/codemate/
|   |-- cli/                  # CLI entry, slash commands, interactive loop
|   |-- agent/                # Agent loop, planning, conversation state
|   |-- llm/                  # Model client abstraction and providers
|   |-- tool/                 # Tool Registry, tool specs, tool results
|   |-- policy/               # Path guard, command guard, approval, audit
|   |-- render/               # Output rendering and streaming display
|   `-- config/               # Runtime configuration loading
|-- src/main/resources/
|   `-- prompts/              # Prompt templates
|-- src/test/java/            # Unit tests
|-- .env.example
|-- .gitignore
`-- pom.xml
```

## Execution Flow

```text
User input
  -> CLI routes slash commands or normal tasks
  -> Agent builds prompt, history, and project context
  -> LLM client sends messages and tool specifications
  -> Model returns content or tool calls
  -> Tool Registry dispatches requested tools
  -> Policy layer checks paths, commands, approval, and audit
  -> Tool results are appended to the conversation
  -> Renderer streams the final response
```

## Design Principles

- Keep the agent loop small and observable.
- Keep model providers behind a stable interface.
- Keep tools explicit, schema-driven, and auditable.
- Keep filesystem and shell access inside policy boundaries.
- Keep sensitive runtime values outside Git.
- Keep documentation aligned with shipped behavior.

## Roadmap

See [docs/development-plan.md](docs/development-plan.md).

Planned extensions:

- RAG-based code retrieval
- MCP dynamic tools
- Browser automation integration
- Side-Git snapshots
- Runtime API
- Image input
- Multi-Agent collaboration
- Skill system
