# AGENTS.md

This file is the entry point for agents working in this repository. Detailed architecture lives in `docs/architecture.md`; delivery phases live in `docs/development-plan.md`.

## Project

- Name: codeMate
- Type: Java AI Agent CLI
- Goal: provide a terminal-first agent for software engineering workflows.

## Commands

```bash
mvn test
mvn package
java -jar target/codemate-0.1.0-SNAPSHOT.jar
```

## Information Priority

1. Actual code behavior
2. `AGENTS.md`
3. `CODEMATE.md`
4. `README.md`
5. `docs/development-plan.md`

## Rules

- Keep runtime credentials outside Git.
- Do not commit `.env`, generated archives, logs, or `target/`.
- Update README or docs when behavior changes.
- Prefer small, focused commits.
- Complete core execution flow before adding optional extensions.

## Module Map

| Area | Path |
| --- | --- |
| CLI input | `src/main/java/com/codemate/cli` |
| Agent loop | `src/main/java/com/codemate/agent` |
| Model clients | `src/main/java/com/codemate/llm` |
| Tool calls | `src/main/java/com/codemate/tool` |
| Policy layer | `src/main/java/com/codemate/policy` |
| Rendering | `src/main/java/com/codemate/render` |
| Configuration | `src/main/java/com/codemate/config` |

## Delivery Boundary

Core:

- ReAct loop
- Tool Registry
- File tools
- Command tool
- Policy layer
- Plan mode
- Project context
- JLine interaction
- Tests and documentation

Optional extensions:

- MCP
- RAG
- Browser automation
- Image input
- Side-Git snapshots
- Runtime API
- Multi-Agent
- Skill system
