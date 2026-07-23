package com.codemate.render;

import com.codemate.agent.ContextStats;
import com.codemate.config.AppConfig;
import com.codemate.config.ModelProfile;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * codeMate's default inline terminal presentation.
 * It keeps the regular terminal scrollback while giving the CLI a focused visual hierarchy.
 */
public final class InlineRenderer implements Renderer {
    private static final String RESET = "\u001B[0m";
    private static final String TEAL = "\u001B[38;5;44m";
    private static final String BLUE = "\u001B[38;5;75m";
    private static final String MUTED = "\u001B[38;5;246m";
    private static final String GREEN = "\u001B[38;5;78m";
    private static final String YELLOW = "\u001B[38;5;221m";
    private static final String RED = "\u001B[38;5;210m";
    private static final String BOLD = "\u001B[1m";

    private final PrintStream output;
    private boolean assistantStarted;
    private String activeModel = "";
    private Path workspace = Path.of(".").toAbsolutePath().normalize();

    public InlineRenderer(PrintStream output) {
        this.output = Objects.requireNonNull(output, "output");
    }

    @Override
    public void startup(AppConfig config) {
        activeModel = config.model();
        output.println(color(TEAL + BOLD, "╭─ codeMate v0.1 ───────────────────────────────────────────────────────────╮"));
        output.println(color(TEAL, "│") + "                                                                    " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "             " + color(BOLD, "欢迎回来！") + "                 "
                + color(TEAL, "│") + "  " + color(BLUE + BOLD, "快速开始") + "                         " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "                                      "
                + color(TEAL, "│") + "  /model    切换模型                    " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "              .--------.              "
                + color(TEAL, "│") + "  /help     查看命令                    " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "              |  <>    |              "
                + color(TEAL, "│") + "  /clear    清空会话                    " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "            --|________|--            "
                + color(TEAL, "│") + "                                      " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "                /    \\                "
                + color(TEAL, "│") + "  " + color(BLUE + BOLD, "当前状态") + "                            " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "                                      "
                + color(TEAL, "│") + "  " + color(GREEN, config.model()) + "              " + color(TEAL, "│"));
        output.println(color(TEAL, "│") + "     " + color(MUTED, config.provider() + " / " + config.model()) + ""
                + "                       " + color(TEAL, "│") + "  " + color(MUTED, "ReAct 已就绪") + "                        " + color(TEAL, "│"));
        output.println(color(TEAL, "╰────────────────────────────────────────────────────────────────────────────╯"));
    }

    @Override
    public void helpHint() {
        output.println("  " + color(MUTED, "输入 /help 查看所有命令，/model 切换模型。"));
        divider();
    }

    @Override
    public void help() {
        output.println(color(BLUE + BOLD, "命令"));
        output.println("  /model                 展示并选择模型");
        output.println("  /model <模型名>        切换模型");
        output.println("  /model init            创建本地模型档案模板");
        output.println("  /cd [目录]             查看或切换工作目录");
        output.println("  /context               查看会话上下文用量");
        output.println("  /clear                 清空当前会话");
        output.println("  /help                  查看命令");
        output.println("  /exit                  退出 codeMate");
        statusLine("空闲");
    }

    @Override
    public void modelStatus(String activeProfile, AppConfig config) {
        output.println(color(BLUE + BOLD, "当前模型"));
        output.println("  档案: " + activeProfile);
        output.println("  模型: " + config.model());
        output.println("  地址: " + color(MUTED, config.baseUrl()));
    }

    @Override
    public void modelProfiles(String activeProfile, List<ModelProfile> profiles) {
        output.println(color(BLUE + BOLD, "可用模型"));
        for (ModelProfile profile : profiles) {
            String marker = profile.name().equals(activeProfile) ? color(GREEN, "●") : color(MUTED, "○");
            output.println("  " + marker + " " + profile.config().model() + color(MUTED,
                    "  (" + profile.name() + " / " + profile.config().provider() + ")"));
        }
        output.println("  " + color(MUTED, "输入 /model <模型名> 切换，或在 /model 后按 Tab 选择。"));
    }

    @Override
    public void modelSwitched(String profileName, AppConfig config) {
        activeModel = config.model();
        output.println(color(GREEN + BOLD, "已切换到 " + config.model())
                + color(MUTED, "  (" + profileName + ")，会话已重新开始。"));
        statusLine("空闲");
    }

    @Override
    public void modelConfigPath(Path path) {
        output.println(color(GREEN, "已创建本地模型档案：") + path);
        output.println(color(MUTED, "填写 Key 后使用 /model 选择。"));
    }

    @Override
    public void workspaceChanged(Path workspace) {
        this.workspace = workspace.toAbsolutePath().normalize();
        output.println(color(GREEN, "工作目录已切换：") + this.workspace);
        statusLine("空闲");
    }

    @Override
    public void contextStatus(ContextStats stats) {
        output.println(color(BLUE + BOLD, "当前上下文"));
        output.println("  消息: " + stats.messageCount() + " 条");
        output.println("  用量: " + stats.characters() + " / " + stats.maxCharacters()
                + " 字符 (" + stats.usagePercent() + "%)");
        output.println("  已裁剪: " + stats.trimmedTurns() + " 轮旧对话");
        statusLine("空闲");
    }

    @Override
    public void prompt() {
        output.print(inputPrompt());
        output.flush();
    }

    @Override
    public String inputPrompt() {
        return color(TEAL + BOLD, "* ");
    }

    @Override
    public void sessionCleared() {
        output.println(color(GREEN, "会话已清空。") + color(MUTED, " 长期配置保持不变。"));
        statusLine("空闲");
    }

    @Override
    public void goodbye() {
        output.println(color(MUTED, "codeMate 已退出。"));
    }

    @Override
    public void unknownCommand(String command) {
        output.println(color(YELLOW, "未知命令：") + command + color(MUTED, "  输入 /help 查看可用命令。"));
    }

    @Override
    public void agentNotReady(String input) {
        output.println(color(YELLOW, "Agent 尚未就绪：") + input);
    }

    @Override
    public void toolExecuting(String name, String arguments) {
        output.println();
        output.println(color(BLUE + BOLD, "工具调用") + " " + color(MUTED, name + " " + arguments));
    }

    @Override
    public void toolCompleted(String name, boolean error) {
        output.println(color(error ? RED : GREEN, error ? "工具失败：" : "工具完成：") + name);
    }

    @Override
    public void assistantDelta(String delta) {
        if (!assistantStarted) {
            assistantStarted = true;
            output.println();
            output.println(color(TEAL + BOLD, "codeMate"));
            output.print(color(TEAL, "┃ "));
        }
        output.print(delta);
        output.flush();
    }

    @Override
    public void assistantDone() {
        if (assistantStarted) {
            output.println();
            assistantStarted = false;
        }
        statusLine("空闲");
    }

    @Override
    public void error(String message) {
        output.println(color(RED + BOLD, "错误：") + message);
        statusLine("需要处理");
    }

    @Override
    public PrintStream stream() {
        return output;
    }

    private void divider() {
        output.println(color(MUTED, "────────────────────────────────────────────────────────────────────────────"));
    }

    private void statusLine(String phase) {
        divider();
        output.println(color(MUTED, "  codeMate") + "  " + color(BLUE, activeModel) + "  "
                + color(GREEN, phase) + "  " + color(MUTED, workspace.toString()));
    }

    private static String color(String style, String text) {
        return style + text + RESET;
    }
}
