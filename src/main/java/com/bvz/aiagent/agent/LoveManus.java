package com.bvz.aiagent.agent;

import com.bvz.aiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class LoveManus extends ToolCallAgent {

    /**
     * LoveManus 是面向复杂任务的工具型 Agent。
     * 这里将本地工具和 MCP 工具合并后统一交给父类管理。
     */
    public LoveManus(
            ToolCallback[] allTools,
            SyncMcpToolCallbackProvider toolCallbackProvider,
            ChatModel dashscopeChatModel) {
        super(mergeTools(allTools, toolCallbackProvider.getToolCallbacks()));
        this.setName("LoveManus");

        String systemPrompt = """
                你是 LoveManus，一个会主动完成任务的智能体。
                当用户请求当前网页信息、文件、PDF、下载、图片、地图、终端执行等能力时，优先使用工具，不要靠猜测回答。
                只有在对应工具真正执行成功后，才能声称“已经生成文件 / 已完成搜索 / 已找到图片 / 已下载资源”。
                如果某项能力不可用，必须明确说明原因，不允许假装任务已经完成。
                如果用户要求的是最终 PDF、图片或其他指定产物，不要擅自生成额外的 txt / 清洗文本 / 中间缓存文件，除非用户明确要求。
                如果最终产物里需要呈现网络图片，优先先把图片下载到本地，再在最终产物里引用本地图片，避免在生成 PDF 时临时联网抓图。
                对中文用户请求，优先使用中文总结工具结果。
                """;
        this.setSystemPrompt(systemPrompt);

        String nextStepPrompt = """
                请主动选择合适的工具并继续执行，除非你真的被阻塞，否则不要停下来征求确认。
                如果任务依赖联网信息、地图、图片、文件或 PDF，必须先调用对应工具，再总结结果。
                如果用户要求“结合网络图片”，先搜索图片资源；如果最终产物是 PDF，优先调用 downloadResource 下载图片到本地，再把本地图片嵌入 PDF，不要只放外链。
                如果用户要求输出 PDF 或其他最终文件，先整理内容，再调用生成工具；只有成功后，才能报告保存路径。
                若 PDF 生成失败，优先直接修正内容并重试 PDF 工具，不要绕路生成多余的 txt 中间文件。
                如果任务已经完成，请调用 terminate 工具结束，而不是继续空转。
                """;
        this.setNextStepPrompt(nextStepPrompt);
        this.setMaxSteps(20);

        // 初始化 ChatClient，并挂载日志 Advisor，方便观察模型请求、工具调用和返回结果。
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }

    /**
     * 合并本地工具和 MCP 工具，统一交给 ToolCallAgent 使用。
     */
    private static ToolCallback[] mergeTools(ToolCallback[] localTools, ToolCallback[] mcpTools) {
        return Stream.concat(Arrays.stream(localTools), Arrays.stream(mcpTools))
                .toArray(ToolCallback[]::new);
    }
}
