# CODEMATE.md

这是 codeMate 的项目级上下文文件，后续会在启动时注入 Agent system prompt。

## 项目规则

- 本项目以学习 Agent 工程化为主，不追求第一天就实现所有高级功能。
- 优先完成主链路：CLI -> Agent -> LLM -> ToolRegistry -> Policy -> Renderer。
- 可以参考 PaiCLI 的设计思路，但实现、命名、文档和 UI 风格保持独立。
- 每天提交并推送到 GitHub 和 Gitee。
- 所有 API Key 只能放在 `.env`，不能提交到 Git。

## 当前目标

第一个月完成一个可运行、可演示、可写简历的 Java AI Agent CLI。
