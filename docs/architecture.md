# codeMate 架构说明

本文件解释 codeMate 的整体流程、模块职责和学习重点。阅读时可以对照 PaiCLI 的 `Main -> Agent -> ToolRegistry -> Policy -> Renderer` 思路，但 codeMate 会保持更小、更容易讲清楚的实现。

## 一句话架构

codeMate 是一个终端里的 AI Agent 编程助手：CLI 接收用户输入，Agent 调用 LLM，LLM 决定是否调用工具，工具执行后把结果回灌给 LLM，最终输出回答。

## 总流程

```text
Main
  -> CommandParser
    -> slash command handler
    -> Agent.run(userInput)
      -> PromptBuilder
      -> LlmClient.chat(messages, tools)
      -> ToolRegistry.execute(toolCall)
        -> Policy checks
        -> Concrete tool
        -> AuditLog
      -> append tool result
      -> continue or finish
  -> Renderer
```

## 模块职责

### cli

职责：

- 程序入口
- 读取用户输入
- 判断 slash 命令还是普通任务
- 组织 Agent、ToolRegistry、Renderer、Config

面试重点：

> CLI 层不应该包含大段业务逻辑，它负责调度。真正的 Agent 逻辑、工具逻辑和安全逻辑要拆出去，避免 Main 变成巨型类。

### agent

职责：

- 维护 conversation history
- 组装 system prompt
- 执行 ReAct 循环
- 控制最大迭代次数
- 将工具结果回灌给模型

核心概念：

```text
LLM response:
  content -> 最终回答
  tool_calls -> 调用工具并继续
```

面试重点：

> ReAct 的关键不是“循环调用模型”，而是让模型在推理、工具调用和观察结果之间形成闭环。

### llm

职责：

- 屏蔽不同模型 API 差异
- 提供统一 `LlmClient`
- 支持 OpenAI-compatible 请求格式
- 支持流式输出和 tool calls

建议接口：

```java
public interface LlmClient {
    ChatResponse chat(ChatRequest request);
    String provider();
    String model();
}
```

面试重点：

> 多模型接入时，不应该让 Agent 直接依赖某一家厂商 API。统一接口可以降低替换模型的成本。

### tool

职责：

- 定义工具 schema
- 注册工具
- 根据 tool name 分发执行
- 返回结构化 ToolResult

第一批工具：

- `read_file`
- `write_file`
- `list_dir`
- `glob_files`
- `grep_code`
- `execute_command`

面试重点：

> Tool Registry 是 Agent 从“聊天”走向“能干活”的关键。工具 schema 告诉模型能做什么，工具执行层负责把模型意图变成真实操作。

### policy

职责：

- 路径围栏
- 命令黑名单
- 危险工具审批
- 审计日志
- 敏感参数脱敏

面试重点：

> Agent 产品不能让 LLM 直接操作文件系统和 Shell。安全层必须独立于模型，即使模型要求危险操作，策略层也可以拒绝。

### render

职责：

- 普通输出
- 流式输出
- 工具调用展示
- 错误展示

第一版只做简洁 UI：

```text
◆ tool read_file
  path: README.md
  status: done
```

面试重点：

> Renderer 抽象让 Agent 不关心输出形态，后续可以从 plain console 升级到 JLine 或完整 TUI。

### config

职责：

- `.env` 读取
- provider/model/baseUrl/apiKey
- 本地配置文件
- API Key 脱敏展示
- 默认值和非法数字回退

面试重点：

> 配置读取要避免把密钥写进 Git，`.env.example` 只放占位符。

## 数据结构草案

```text
Message
  role: system | user | assistant | tool
  content
  toolCalls
  toolCallId

ToolSpec
  name
  description
  parameters schema

ToolCall
  id
  name
  arguments

ToolResult
  success
  content
  error
```

## 与 PaiCLI 的关系

可参考的思路：

- 分层架构
- ReAct 主链路
- ToolRegistry
- PathGuard / CommandGuard
- HITL 审批
- Prompt 分层
- Plan DAG
- Memory 和项目上下文

需要重新实现的部分：

- 项目名、包名、README 文案
- UI 风格
- 类结构细节
- 测试用例
- Demo 场景

目标不是复制一个 PaiCLI，而是通过重写一个小而清晰的 Agent CLI，真正掌握工程流程。
