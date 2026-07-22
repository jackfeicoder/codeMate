# codeMate Architecture

This document describes the execution flow, module responsibilities, and core data structures used by codeMate.

## Overview

codeMate is a terminal AI Agent CLI. The CLI receives user input, the Agent calls the model, the model may request tools, tools execute under policy checks, and results are returned to the model until the final response is ready.

## Flow

```text
Main
  -> PlainRenderer
  -> CliApplication
  -> CliCommandParser
    -> slash command handler
    -> Agent.run(userInput)
      -> PromptBuilder
      -> LlmClient.stream(messages, listener)
      -> ToolRegistry.execute(toolCall)
        -> Policy checks
        -> Concrete tool
        -> AuditLog
      -> append tool result
      -> continue or finish
  -> Renderer
```

## Modules

### cli

Responsibilities:

- Program entry
- Interactive input loop
- Slash command routing
- Agent, Tool Registry, Renderer, and configuration wiring

The CLI layer should orchestrate components without owning large business logic.

Current implementation:

- `Main`: loads runtime configuration and starts `CliApplication`
- `CliApplication`: owns the command loop and current session state
- `CliCommandParser`: classifies slash commands and normal tasks
- `CommandType` and `ParsedCommand`: command parsing result model

### agent

Responsibilities:

- Conversation state
- System prompt assembly
- ReAct loop
- Step limit
- Tool result feedback

Core behavior:

```text
LLM response:
  content -> final answer
  tool_calls -> execute tools and continue
```

### llm

Responsibilities:

- Hide provider-specific API differences
- Expose a stable `LlmClient`
- Support OpenAI-compatible chat completion requests
- Parse model responses into project response objects

Current interface:

```java
public interface LlmClient {
    LlmResponse complete(List<LlmMessage> messages);
    LlmResponse stream(List<LlmMessage> messages, StreamListener listener);
}
```

Current implementation:

- `LlmMessage`: role and content pair
- `LlmResponse`: assistant content and token usage wrapper
- `StreamListener`: receives assistant content deltas
- `OpenAiCompatibleClient`: non-streaming and SSE `/chat/completions` client
- `LlmClientFactory`: creates a configured client from `AppConfig`

### tool

Responsibilities:

- Tool schema definitions
- Tool registration
- Tool dispatch
- Structured tool results

Initial tools:

- `read_file`
- `write_file`
- `list_dir`
- `glob_files`
- `grep_code`
- `execute_command`

### policy

Responsibilities:

- Path boundary checks
- Command denylist
- Approval workflow
- Audit log
- Sensitive argument redaction

The policy layer is independent from model output. A requested action can be denied even when the model asks for it.

### render

Responsibilities:

- Normal output
- Streaming output
- Tool-call display
- Error display

Current implementation:

- `Renderer`: output boundary used by CLI and future Agent components
- `PlainRenderer`: simple PrintStream-backed terminal renderer
- `assistantDelta`: renders streaming assistant content fragments

Initial display style:

```text
tool read_file
  path: README.md
  status: done
```

### config

Responsibilities:

- `.env` loading
- Provider, model, base URL, and local credential loading
- Masked credential display
- Default values and invalid value fallback

Sensitive runtime values must remain outside Git.

## Data Structures

```text
LlmMessage
  role: system | user | assistant | tool
  content

LlmResponse
  content
  inputTokens
  outputTokens

ToolSpec
  name
  description
  parameters schema

ToolCall
  ref
  name
  arguments

ToolResult
  success
  content
  error
```

## Extension Points

- More model providers
- More tools
- Plan-and-Execute
- RAG retrieval
- MCP dynamic tools
- Browser automation
- Runtime API
