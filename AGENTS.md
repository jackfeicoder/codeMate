# AGENTS.md

本仓库给 AI Agent / 新线程使用的首读入口。详细设计见 `docs/architecture.md`，开发节奏见 `docs/development-plan.md`。

## 项目定位

- 项目名：codeMate
- 类型：Java AI Agent CLI
- 目标：参考 PaiCLI / Claude Code 的工程思路，从 0 到 1 实现一个可学习、可演示、可写简历的 Agent 编程助手。

## 常用命令

```bash
mvn test
mvn package
java -jar target/codemate-0.1.0-SNAPSHOT.jar
```

## 信息优先级

1. 代码实际行为
2. `AGENTS.md`
3. `CODEMATE.md`
4. `README.md`
5. `docs/development-plan.md`

## 开发硬规则

- 改行为同步 README 或 docs。
- 不提交 `.env`、API Key、token、`target/`。
- 优先小步提交，每天 push 到 GitHub 和 Gitee。
- 参考 PaiCLI 的架构思想，但不要逐行复制源码。
- 先完成核心链路，再扩展高级功能。

## 模块导航

| 任务 | 先看 |
| --- | --- |
| CLI 输入 | `src/main/java/com/codemate/cli` |
| Agent 循环 | `src/main/java/com/codemate/agent` |
| 模型接入 | `src/main/java/com/codemate/llm` |
| 工具调用 | `src/main/java/com/codemate/tool` |
| 安全策略 | `src/main/java/com/codemate/policy` |
| 输出展示 | `src/main/java/com/codemate/render` |
| 配置读取 | `src/main/java/com/codemate/config` |

## 第一个月边界

必须完成：

- ReAct
- Tool Registry
- 文件工具
- 命令工具
- 安全策略
- Plan 模式
- 简单 Memory
- JLine 基础交互
- 测试和 README

暂不强行完成：

- MCP
- RAG
- Chrome DevTools
- 图片输入
- Side-Git
- Runtime API
- 微信通道
