package com.bvz.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TerminalOperationToolTest {

    @Test
    public void testExecuteTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        // Windows 环境下使用 dir 替代 ls
        String command = "dir";
        String result = tool.executeTerminalCommand(command);

        assertNotNull(result, "命令执行结果不应为 null");
        assertFalse(result.isEmpty(), "命令执行结果不应为空");
        assertFalse(result.startsWith("Error"), "命令执行不应以 Error 开头: " + result);
        assertFalse(result.contains("Command execution failed"),
                "命令执行不应失败: " + result);
    }

    @Test
    public void testExecuteEchoCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "echo Hello World";
        String result = tool.executeTerminalCommand(command);

        assertNotNull(result);
        assertTrue(result.contains("Hello World"),
                "echo 命令应返回原文本，实际结果: " + result);
    }
}
