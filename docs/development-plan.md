# codeMate Development Plan

This roadmap tracks product delivery in small, verifiable phases.

## Daily Workflow

```bash
git status
mvn test
git add .
git commit -m "phase: concise change summary"
git push github main
git push gitee main
```

## Phase 1: Core Agent Flow

Goal: normal chat flow plus the first model-callable tool.

| Step | Scope | Output |
| --- | --- | --- |
| 1 | Project initialization | Maven skeleton, README, docs, minimal Main |
| 2 | Runtime configuration | `.env` loading, `AppConfig`, provider configuration |
| 3 | Model abstraction | `LlmClient`, messages, OpenAI-compatible client |
| 4 | CLI loop | `/help`, `/exit`, `/clear`, history |
| 5 | Streaming output | SSE parsing, `Renderer.stream`, error handling |
| 6 | Agent loop | `Agent`, system prompt, step limit |
| 7 | First tool | `Tool`, `ToolRegistry`, `read_file`, tool feedback |

Acceptance:

```text
User asks for a project summary
Agent reads README
Agent returns a concise project summary
```

Delivered so far:

- Step 1: project skeleton, package layout, docs, minimal CLI
- Step 2: environment configuration and masked credential display
- Step 3: `LlmClient`, `LlmMessage`, `LlmResponse`, `LlmClientFactory`, and OpenAI-compatible chat completion client
- Step 4: `CliApplication`, `CliCommandParser`, basic command loop, `/help`, `/clear`, `/exit`, and `/quit`
- Step 5: `Renderer` abstraction and `PlainRenderer` terminal output
- Step 6: SSE streaming support in `LlmClient`, `OpenAiCompatibleClient`, and `Renderer`
- Step 7: `Agent`, `PromptLoader`, conversation history, streamed assistant output, and `/clear` history reset

## Phase 2: Tool System and Policy

Goal: controlled file and command operations.

| Step | Scope | Output |
| --- | --- | --- |
| 8 | File tools | `list_dir`, `write_file`, `glob_files` |
| 9 | Code search | `grep_code`, `rg` first, Java fallback |
| 10 | Command execution | `execute_command`, timeout, output limit |
| 11 | Path policy | `PathGuard`, project-root boundary |
| 12 | Command policy | `CommandGuard`, dangerous command denylist |
| 13 | Approval | `ApprovalPolicy`, `HitlHandler`, yes/no confirmation |
| 14 | Audit | `AuditLog`, redacted risky tool records |

Acceptance:

```text
Agent requests a file write
CLI asks for approval
Approved action is executed and audited
```

## Phase 3: Planning and Context

Goal: support multi-step engineering tasks.

| Step | Scope | Output |
| --- | --- | --- |
| 15 | Model factory | `LlmClientFactory`, `/model` |
| 16 | Config command | `/config`, provider display and updates |
| 17 | Planner | JSON plan generation, `Task` |
| 18 | DAG | `ExecutionPlan`, topological order |
| 19 | Plan execution | `/plan <task>`, confirmation, step execution |
| 20 | Project context | `CODEMATE.md` loading |
| 21 | Conversation management | history trimming and context boundaries |

Acceptance:

```text
/plan add a utility and tests
-> plan is generated
-> user approves
-> agent executes each step
```

## Phase 4: Product Polish

Goal: make the CLI reliable and pleasant to operate.

| Step | Scope | Output |
| --- | --- | --- |
| 22 | JLine shell | input history, stable prompt, Ctrl+C/EOF |
| 23 | Completion | slash command completion |
| 24 | Tool display | start/end status, argument summary |
| 25 | Tests I | parser, path policy, command policy, factory |
| 26 | Tests II | tools, execution plan, context |
| 27 | README | setup, commands, architecture links |
| 28 | Demo scripts | reproducible product scenarios |
| 29 | Regression | bug fixes and cleanup |
| 30 | Release prep | version notes and package output |

## Deferred Extensions

- Full MCP support
- SQLite-backed RAG
- Browser automation
- Image input
- Side-Git snapshots
- Runtime API
- Multi-Agent orchestration
- Skill system
