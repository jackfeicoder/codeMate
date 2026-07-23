# codeMate

codeMate 是一个从 0 到 1 实现的 Java AI Agent CLI 学习项目。它参考 PaiCLI / Claude Code / Codex 这类 Agent 编程助手的工程思路，但使用新的项目名、包名、文档和交互风格重新实现。

本项目有两个目标：

1. 做出一个可运行、可演示、可持续扩展的 AI Agent CLI。
2. 通过每天构建和提交，系统学习 Agent 工程化流程，以便应对实习和校招面试。

## 项目定位

codeMate 不是简单的“调用大模型 API 聊天工具”，而是一个面向开发者的 Agent CLI。它会逐步具备：

- ReAct Agent 循环
- 多模型 Provider 接入
- 文件读取、写入、目录列表、代码搜索、命令执行
- Tool Registry 工具注册系统
- 路径围栏、命令黑名单、HITL 人工审批、审计日志
- Plan-and-Execute 任务规划
- 简单 Memory 和项目上下文注入
- JLine 终端交互、slash 命令、输入历史和工具调用展示

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 语言 | Java 17 |
| 构建 | Maven |
| HTTP | OkHttp |
| JSON | Jackson |
| 终端交互 | JLine |
| 测试 | JUnit 5 |
| 后续可选 | SQLite, JavaParser, JGit, MCP, RAG |

## 当前状态

当前是 Day 2 配置系统阶段：

- Maven 项目已创建
- 基础包结构已创建
- README 和学习文档已创建
- 最小 `Main` 入口已创建
- 可读取 `.env` / 系统环境变量中的模型配置
- 启动时展示 provider、model、项目上下文和脱敏 API Key

运行：

```bash
copy .env.example .env
mvn test
mvn package
java -jar target/codemate-0.1.0-SNAPSHOT.jar
```

配置项：

| 变量 | 作用 | 默认值 |
| --- | --- | --- |
| `CODEMATE_PROVIDER` | 当前模型供应商 | `deepseek` |
| `CODEMATE_MODEL` | 当前模型名 | `deepseek-chat` |
| `CODEMATE_BASE_URL` | OpenAI-compatible API Base URL | `https://api.deepseek.com/v1` |
| `CODEMATE_API_KEY` | 模型 API Key，不会打印明文 | 空 |
| `CODEMATE_PROJECT_CONTEXT` | 项目上下文文件 | `CODEMATE.md` |
| `CODEMATE_MAX_AGENT_STEPS` | Agent 最大循环步数 | `8` |

## 目录结构

```text
codeMate/
├── docs/
│   ├── architecture.md       # 整体流程、模块职责、数据流
│   ├── development-plan.md   # 30 天开发路线
├── src/main/java/com/codemate/
│   ├── cli/                  # CLI 入口、slash 命令、交互循环
│   ├── agent/                # ReAct Agent、Plan Agent、会话历史
│   ├── llm/                  # LlmClient、多模型 Provider
│   ├── tool/                 # Tool Registry、工具定义、工具结果
│   ├── policy/               # 路径安全、命令安全、审批和审计
│   ├── render/               # 输出渲染、流式输出、工具 UI
│   └── config/               # .env 和本地配置读取
├── src/main/resources/
│   └── prompts/              # Prompt 模板
├── src/test/java/            # 单元测试
├── .env.example
├── .gitignore
└── pom.xml
```

## 核心执行流程

```text
用户输入
  -> CLI 判断是否为 slash 命令
  -> 普通任务进入 Agent
  -> Agent 组装 system prompt + history + project context
  -> LlmClient 调用模型
  -> 模型返回 content 或 tool_calls
  -> ToolRegistry 执行工具
  -> Policy 检查路径、命令、审批、审计
  -> 工具结果回灌给模型
  -> 最终答案由 Renderer 输出
```

## 开发原则

- 先主链路，后高级功能。
- 每天完成一个明确目标，每天 git commit，每天 push 到 GitHub 和 Gitee。
- 可以参考 PaiCLI 的架构思想，但不逐行复制代码。
- README 和 docs 随代码同步维护。
- 所有密钥只放 `.env`，不进入 Git。
- 面试导向：每个模块都要能解释“为什么这么设计”。

## 远程仓库

- GitHub: https://github.com/jackfeicoder/codeMate
- Gitee: https://gitee.com/jackfei2545/codeMate.git

## 模型档案

codeMate 支持保存多个本地模型档案，并在运行时切换：

```text
/model                 # 展示档案列表并选择
/model list            # 列出档案
/model use ccswitch    # 切换到指定档案
/model init            # 生成本地档案模板
```

`/model init` 会在 `.codemate/models.properties` 创建配置模板。该文件已被 Git 忽略，真实 API Key 只应填写在该本地文件或 `.env` 中。模型切换后会重建 LLM Client，并清空当前会话上下文。

终端输入使用 JLine 补全。输入 `/m` 后按 `Tab` 会列出匹配的 slash 命令；输入 `/model` 后按 `Tab` 会展开模型下拉列表，选择后会补全为切换命令；输入 `/model d` 后按 `Tab` 会按前缀展示模型档案，例如 `deepseek`。候选项可以通过 `Tab` 或方向键选择并确认。

## Roadmap

第一个月目标见 [docs/development-plan.md](docs/development-plan.md)。

长期扩展方向：

- RAG 代码检索
- MCP 动态工具
- Chrome DevTools 集成
- Side-Git 快照回滚
- Runtime API
- 图片输入
- Multi-Agent 协作
- Skill 系统
