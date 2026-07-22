# codeMate 30 天开发路线

这份路线以“每天可提交、每周可演示、月底可写简历”为目标。项目参考 PaiCLI 的工程分层，但重新实现为 codeMate。

## 每天固定流程

```bash
git status
mvn test
git add .
git commit -m "dayXX: 描述当天成果"
git push github main
git push gitee main
```

如果当天代码没有完全完成，也要提交可编译的中间状态，并在 README 或 docs 记录下一步。

## 第 1 周：核心 Agent 链路

目标：能聊天，能让模型调用第一个工具读取文件。

| Day | 目标 | 产出 |
| --- | --- | --- |
| 1 | 项目初始化 | Maven 骨架、README、docs、最小 Main |
| 2 | 配置系统 | `.env` 读取、`AppConfig`、Provider 配置 |
| 3 | LLM 抽象 | `LlmClient`、Message、OpenAI-compatible Client |
| 4 | CLI 主循环 | `/help`、`/exit`、`/clear`、history |
| 5 | 流式输出 | SSE 解析、Renderer.stream、错误处理 |
| 6 | ReAct 雏形 | `Agent`、system prompt、最大迭代保护 |
| 7 | 第一个工具 | `Tool`、`ToolRegistry`、`read_file`、工具回灌 |

第 1 周验收：

```text
用户：帮我看看 README.md 是什么项目
Agent：调用 read_file -> 读取 README -> 总结
```

## 第 2 周：工具系统和安全边界

目标：Agent 能真实操作项目文件，但所有危险能力都有边界。

| Day | 目标 | 产出 |
| --- | --- | --- |
| 8 | 文件工具 | `list_dir`、`write_file`、`glob_files` |
| 9 | 代码搜索 | `grep_code`，优先 `rg`，fallback Java 扫描 |
| 10 | 命令执行 | `execute_command`、超时、输出截断 |
| 11 | 路径安全 | `PathGuard`，项目根内访问限制 |
| 12 | 命令安全 | `CommandGuard`，危险命令黑名单 |
| 13 | HITL 审批 | `ApprovalPolicy`、`HitlHandler`、`y/n` 确认 |
| 14 | 审计日志 | `AuditLog`，危险工具调用记录和脱敏 |

第 2 周验收：

```text
用户：帮我创建 hello.txt
Agent：请求 write_file
CLI：是否允许写入？
用户：y
文件写入成功
```

## 第 3 周：多模型、Plan、Memory

目标：项目从“工具型 Agent”升级为“有工程架构的 Agent CLI”。

| Day | 目标 | 产出 |
| --- | --- | --- |
| 15 | 多模型工厂 | `LlmClientFactory`、`/model` |
| 16 | 配置命令 | `/config`，显示和修改 Provider |
| 17 | Planner | 模型生成 JSON plan，`Task` |
| 18 | DAG | `ExecutionPlan`、拓扑排序、依赖检查 |
| 19 | Plan 执行 | `/plan <任务>`、计划确认、按步骤执行 |
| 20 | 简单 Memory | `/save`、`/memory`、JSON 长期记忆 |
| 21 | 项目上下文 | 读取 `CODEMATE.md` 注入 system prompt |

第 3 周验收：

```text
/plan 帮我新增一个工具类并写测试
-> 生成计划
-> 用户确认
-> Agent 按步骤读文件、写文件、执行测试
```

## 第 4 周：产品化、测试、面试材料

目标：让项目像完整作品，而不是零散代码。

| Day | 目标 | 产出 |
| --- | --- | --- |
| 22 | JLine | 输入历史、稳定 prompt、Ctrl+C/EOF |
| 23 | 命令补全 | slash 命令 Tab 补全 |
| 24 | 工具 UI | 工具开始/结束展示、参数摘要 |
| 25 | 测试 1 | Parser、PathGuard、CommandGuard、Factory |
| 26 | 测试 2 | ToolRegistry、ExecutionPlan、Memory |
| 27 | README 完整化 | 功能列表、快速开始、架构图 |
| 28 | Demo 脚本 | 三个演示场景 |
| 29 | 回归打磨 | 修 bug、清理文档、检查密钥 |
| 30 | 简历材料 | 架构说明、学习笔记、项目总结 |

月底验收：

- 能现场演示。
- 能解释核心链路。
- GitHub/Gitee 都有连续提交记录。
- README 清楚，测试能跑。

## 暂不做的功能

第一个月不强行实现以下能力，只写入 Roadmap：

- 完整 MCP
- SQLite 向量 RAG
- Chrome DevTools
- 图片输入
- Side-Git 快照
- Runtime API
- 微信通道
- Multi-Agent
- Skill 系统

这些功能可以在第二阶段继续参考 PaiCLI 实现。
