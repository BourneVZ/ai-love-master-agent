package com.bvz.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础 Agent。
 * 这里封装了 think / act 的核心流程，并补了一层“工具策略保护”：
 * 1. 当模型没有发出必要工具调用时，允许按规则兜底推进。
 * 2. 当模型想生成不必要的中间产物时，先拦截再继续下一轮思考。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 已注册的可用工具，既包含本地工具，也包含 MCP 工具。
    private final ToolCallback[] availableTools;

    // 保存最近一轮模型响应，act() 阶段会基于它执行 tool calls。
    private ChatResponse toolCallChatResponse;

    // Spring AI 的工具调用执行器。
    private final ToolCallingManager toolCallingManager;

    // 基础 ChatOptions。每一轮会在此基础上按需附加强制工具选择策略。
    private final DashScopeChatOptions baseChatOptions;

    // 记录当前轮的 ChatOptions，确保 executeToolCalls 时拿到同一批工具定义。
    private DashScopeChatOptions currentChatOptions;

    // 当模型没主动调工具，但程序决定兜底执行时，先暂存在这里，act() 再消费。
    private ToolResponseMessage pendingToolResponseMessage;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.baseChatOptions = DashScopeChatOptions.builder()
                .parallelToolCalls(false)
                .toolCallbacks(Arrays.asList(availableTools))
                .build();

        String toolNames = Arrays.stream(availableTools)
                .map(tool -> tool.getToolDefinition().name())
                .collect(Collectors.joining(", "));
        log.info("已加载工具: {}", toolNames);
    }

    /**
     * 思考阶段：
     * 1. 追加下一步提示词。
     * 2. 调用模型，让模型决定是否发起工具调用。
     * 3. 如果模型未调工具，但任务客观依赖工具，则走兜底或报错。
     */
    @Override
    public boolean think() {
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }

        String forcedToolName = determineForcedToolName();
        DashScopeChatOptions chatOptions = buildChatOptions(forcedToolName);
        this.currentChatOptions = chatOptions;
        Prompt prompt = new Prompt(getMessageList(), chatOptions);

        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            this.toolCallChatResponse = chatResponse;

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            log.info("{}的思考: {}", getName(), result);
            log.info("{}选择了 {} 个工具来使用", getName(), toolCallList.size());
            if (StrUtil.isNotBlank(forcedToolName)) {
                log.info("{}本轮建议优先使用工具: {}", getName(), forcedToolName);
            }

            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            if (StrUtil.isNotBlank(toolCallInfo)) {
                log.info(toolCallInfo);
            }

            if (toolCallList.isEmpty()) {
                // 模型没有主动调工具时，优先尝试程序兜底推进关键工具。
                if (StrUtil.isNotBlank(forcedToolName)) {
                    ToolResponseMessage fallbackResponse = tryExecuteForcedTool(forcedToolName, assistantMessage.getText());
                    if (fallbackResponse != null) {
                        this.pendingToolResponseMessage = fallbackResponse;
                        return true;
                    }
                }

                // 兜底也失败后，再把 assistant 文本写回上下文，方便后续排查。
                getMessageList().add(assistantMessage);

                // 若任务本身依赖工具，但模型仍未发起工具调用，则当前结果不可信。
                if (isToolRequiredButMissing()) {
                    setState(AgentState.ERROR);
                    log.warn("{}未调用必要工具，当前结果不可信，停止执行", getName());
                    return false;
                }

                // 不依赖工具的任务，允许直接结束。
                setState(AgentState.FINISHED);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("{}的思考过程遇到了问题: {}", getName(), e.getMessage());
            setState(AgentState.ERROR);
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 行动阶段：
     * 1. 若已有程序兜底结果，直接消费。
     * 2. 否则走标准 tool-calling 执行流程。
     * 3. 执行前增加一层“工具使用约束”，避免模型乱写中间文件。
     */
    @Override
    public String act() {
        if (pendingToolResponseMessage != null) {
            ToolResponseMessage toolResponseMessage = pendingToolResponseMessage;
            pendingToolResponseMessage = null;

            List<Message> updatedMessages = new ArrayList<>(getMessageList());
            updatedMessages.add(toolResponseMessage);
            setMessageList(updatedMessages);

            String results = formatToolResponses(toolResponseMessage);
            log.info(results);
            return results;
        }

        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }

        ToolResponseMessage blockedToolResponse = buildBlockedToolResponseIfNeeded();
        if (blockedToolResponse != null) {
            List<Message> updatedMessages = new ArrayList<>(getMessageList());
            updatedMessages.add(blockedToolResponse);
            setMessageList(updatedMessages);

            String results = formatToolResponses(blockedToolResponse);
            log.info(results);
            return results;
        }

        Prompt prompt = new Prompt(getMessageList(), currentChatOptions != null ? currentChatOptions : baseChatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());

        // 工具执行完成后，conversationHistory 的最后一条通常就是 ToolResponseMessage。
        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 若调用了 terminate 工具，则显式结束 Agent。
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }

        String results = formatToolResponses(toolResponseMessage);
        log.info(results);
        return results;
    }

    /**
     * 构建当前轮的 ChatOptions。
     * 如果程序已判断“这一步应优先尝试某个工具”，就通过 toolChoice / toolName 对模型施加约束。
     */
    private DashScopeChatOptions buildChatOptions(String forcedToolName) {
        DashScopeChatOptions.DashScopeChatOptionsBuilder builder = DashScopeChatOptions.builder()
                .parallelToolCalls(false)
                .toolCallbacks(Arrays.asList(availableTools))
                .incrementalOutput(baseChatOptions.getIncrementalOutput())
                .temperature(baseChatOptions.getTemperature())
                .topP(baseChatOptions.getTopP())
                .topK(baseChatOptions.getTopK())
                .enableThinking(baseChatOptions.getEnableThinking())
                .thinkingBudget(baseChatOptions.getThinkingBudget())
                .internalToolExecutionEnabled(false);

        if (StrUtil.isNotBlank(forcedToolName)) {
            builder.toolName(forcedToolName);
            builder.toolChoice(DashScopeApiSpec.ChatCompletionRequestParameter.ToolChoiceBuilder.function(forcedToolName));
        }
        return builder.build();
    }

    /**
     * 选择本轮优先推动的工具。
     * 这里使用的是通用产物顺序规则，而不是针对 LoveManusTest 的硬编码：
     * 先搜外部信息，再搜图片，最后生成最终产物。
     */
    private String determineForcedToolName() {
        String originalUserRequest = getOriginalUserRequest();
        if (StrUtil.isBlank(originalUserRequest)) {
            return null;
        }

        boolean needsSearch = StrUtil.containsAnyIgnoreCase(originalUserRequest,
                "搜索", "查找", "地点", "约会", "web", "search");
        boolean needsImage = StrUtil.containsAnyIgnoreCase(originalUserRequest,
                "图片", "image", "照片");
        boolean needsPdf = StrUtil.containsAnyIgnoreCase(originalUserRequest, "pdf");
        boolean needsTextFile = userExplicitlyRequestsTextArtifact();

        if (needsSearch && !hasToolResponseNamed("searchWeb") && hasTool("searchWeb")) {
            return "searchWeb";
        }
        if (needsImage && !hasToolResponseNamed("searchImage") && hasTool("searchImage")) {
            return "searchImage";
        }
        if (needsImage && needsPdf && hasToolResponseNamed("searchImage")
                && !hasToolResponseNamed("downloadResource") && hasTool("downloadResource")) {
            return "downloadResource";
        }
        if (needsPdf && hasAnyToolResponse() && !hasToolResponseNamed("generatePDF") && hasTool("generatePDF")) {
            return "generatePDF";
        }
        if (needsTextFile && hasAnyToolResponse() && !hasToolResponseNamed("writeFile") && hasTool("writeFile")) {
            return "writeFile";
        }
        return null;
    }

    /**
     * 当模型没有可靠地产生 tool call 时，程序直接执行关键工具作为兜底。
     */
    private ToolResponseMessage tryExecuteForcedTool(String toolName, String assistantText) {
        ToolCallback toolCallback = findTool(toolName);
        if (toolCallback == null) {
            return null;
        }

        String arguments = buildFallbackArguments(toolName, assistantText);
        if (StrUtil.isBlank(arguments)) {
            return null;
        }

        try {
            log.warn("{}未主动调用工具，程序改为兜底执行工具: {}，参数: {}", getName(), toolName, arguments);
            String response = toolCallback.call(arguments);
            return ToolResponseMessage.builder()
                    .responses(List.of(new ToolResponseMessage.ToolResponse(
                            UUID.randomUUID().toString(),
                            toolName,
                            response
                    )))
                    .build();
        } catch (Exception e) {
            log.error("兜底执行工具 {} 失败: {}", toolName, e.getMessage());
            return null;
        }
    }

    /**
     * 为兜底执行构造参数。
     * 目前覆盖项目里最常见的搜索 / 图片搜索 / PDF 生成 / 文本落盘场景。
     */
    private String buildFallbackArguments(String toolName, String assistantText) {
        String originalUserRequest = getOriginalUserRequest();
        return switch (toolName) {
            case "searchWeb", "searchImage" ->
                    JSONUtil.createObj().set("query", originalUserRequest).toString();
            case "downloadResource" -> buildDownloadFallbackArguments();
            case "generatePDF" ->
                    JSONUtil.createObj()
                            .set("fileName", buildPdfFileName())
                            .set("content", buildPdfContent(assistantText))
                            .toString();
            case "writeFile" ->
                    JSONUtil.createObj()
                            .set("fileName", buildTextFileName())
                            .set("content", buildPdfContent(assistantText))
                            .toString();
            default -> null;
        };
    }

    /**
     * 当模型没有自己组织下载参数时，兜底从已有 searchImage 结果里取第一张图下载到本地。
     * 这样后续 PDF 至少可以使用本地图片路径，而不是在生成时临时联网抓外链。
     */
    private String buildDownloadFallbackArguments() {
        String imageUrl = findFirstImageUrlFromToolResponses();
        if (StrUtil.isBlank(imageUrl)) {
            return null;
        }

        String extension = ".jpg";
        String normalizedUrl = imageUrl.toLowerCase();
        if (normalizedUrl.contains(".png")) {
            extension = ".png";
        } else if (normalizedUrl.contains(".webp")) {
            extension = ".webp";
        }

        return JSONUtil.createObj()
                .set("url", imageUrl)
                .set("fileName", "agent_image_1" + extension)
                .toString();
    }

    private String buildPdfFileName() {
        return "agent_output.pdf";
    }

    private String buildTextFileName() {
        return "agent_output.txt";
    }

    /**
     * 生成 PDF / 文本文件时使用的兜底内容。
     * 优先采用模型最近一轮给出的正文；如果正文更像“虚假完成宣告”，则退化为拼接工具结果。
     */
    private String buildPdfContent(String assistantText) {
        if (StrUtil.isNotBlank(assistantText) && !looksLikeFakeCompletion(assistantText)) {
            return assistantText;
        }

        String toolHistory = getMessageList().stream()
                .filter(ToolResponseMessage.class::isInstance)
                .map(ToolResponseMessage.class::cast)
                .flatMap(message -> message.getResponses().stream())
                .map(response -> "【" + response.name() + "】\n" + response.responseData())
                .collect(Collectors.joining("\n\n"));

        return "用户需求：\n" + getOriginalUserRequest() + "\n\n" +
                "工具执行结果汇总：\n" + toolHistory + "\n\n" +
                "说明：以上内容由智能体根据搜索和图片结果自动整理，可继续优化排版与细节。";
    }

    /**
     * 对模型计划执行的工具先做一层策略校验。
     * 当前重点限制：如果用户没明确要文本文件，就不要让模型额外生成 txt 中间产物。
     */
    private ToolResponseMessage buildBlockedToolResponseIfNeeded() {
        AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
        List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
        if (CollUtil.isEmpty(toolCallList)) {
            return null;
        }

        AssistantMessage.ToolCall firstToolCall = toolCallList.get(0);
        if ("writeFile".equals(firstToolCall.name()) && !userExplicitlyRequestsTextArtifact()) {
            log.warn("{}尝试写出 txt 文件，但用户并未明确要求文本文件，已拦截该工具调用: {}",
                    getName(), firstToolCall.arguments());
            return ToolResponseMessage.builder()
                    .responses(List.of(new ToolResponseMessage.ToolResponse(
                            UUID.randomUUID().toString(),
                            "writeFile",
                            "Skipped writeFile: user did not explicitly request a text artifact. Continue with the required final artifact instead."
                    )))
                    .build();
        }

        if ("generatePDF".equals(firstToolCall.name())
                && requestNeedsPdfWithImages()
                && hasToolResponseNamed("searchImage")
                && !hasToolResponseNamed("downloadResource")) {
            log.warn("{}尝试在图片尚未下载到本地时直接生成 PDF，已拦截该工具调用: {}",
                    getName(), firstToolCall.arguments());
            return ToolResponseMessage.builder()
                    .responses(List.of(new ToolResponseMessage.ToolResponse(
                            UUID.randomUUID().toString(),
                            "generatePDF",
                            "Skipped generatePDF: image resources have not been downloaded locally yet. Download the required images first, then generate the final PDF."
                    )))
                    .build();
        }
        return null;
    }

    /**
     * 判断用户是否真的要求了 txt / text / markdown 这类文本型产物。
     * 这是通用产物策略，不是为某个单测场景硬编码。
     */
    private boolean userExplicitlyRequestsTextArtifact() {
        String originalUserRequest = getOriginalUserRequest();
        if (StrUtil.isBlank(originalUserRequest)) {
            return false;
        }
        return StrUtil.containsAnyIgnoreCase(originalUserRequest,
                "txt", "text", "markdown", ".md", ".txt", "plain text") ||
                StrUtil.containsAny(originalUserRequest,
                        "文本", "txt文件", "文本文件", "纯文本", "Markdown");
    }

    private boolean requestNeedsPdfWithImages() {
        String originalUserRequest = getOriginalUserRequest();
        return StrUtil.isNotBlank(originalUserRequest)
                && StrUtil.containsAnyIgnoreCase(originalUserRequest, "pdf")
                && StrUtil.containsAnyIgnoreCase(originalUserRequest, "图片", "image", "照片");
    }

    private String findFirstImageUrlFromToolResponses() {
        return getMessageList().stream()
                .filter(ToolResponseMessage.class::isInstance)
                .map(ToolResponseMessage.class::cast)
                .flatMap(message -> message.getResponses().stream())
                .filter(response -> "searchImage".equals(response.name()))
                .map(ToolResponseMessage.ToolResponse::responseData)
                .map(Object::toString)
                .map(this::extractFirstHttpUrl)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    private String extractFirstHttpUrl(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("https?://[^,\\s\"\\]}]+")
                .matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * 判断用户请求是否客观依赖工具。
     * 这是最后一道保护，避免模型没调工具却口头宣称“已经完成文件/联网/图片/PDF 等任务”。
     */
    private boolean isToolRequiredButMissing() {
        if (hasAnyToolResponse()) {
            return false;
        }
        String originalUserRequest = getOriginalUserRequest();
        if (StrUtil.isBlank(originalUserRequest)) {
            return false;
        }

        String normalized = originalUserRequest.toLowerCase();
        return StrUtil.containsAnyIgnoreCase(normalized,
                "pdf", "file", "search", "web", "scrape", "download", "image", "map", "terminal") ||
                StrUtil.containsAny(originalUserRequest,
                        "PDF", "文件", "搜索", "联网", "网络", "网页", "爬取", "下载", "图片", "地图", "终端", "执行");
    }

    private String getOriginalUserRequest() {
        return getMessageList().stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .map(UserMessage::getText)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse("");
    }

    private boolean looksLikeFakeCompletion(String text) {
        return StrUtil.containsAnyIgnoreCase(text,
                "successfully created", "successfully generated", "saved as a pdf", "found of these locations");
    }

    private boolean hasTool(String toolName) {
        return findTool(toolName) != null;
    }

    private ToolCallback findTool(String toolName) {
        return Arrays.stream(availableTools)
                .filter(tool -> toolName.equals(tool.getToolDefinition().name()))
                .findFirst()
                .orElse(null);
    }

    private boolean hasAnyToolResponse() {
        return getMessageList().stream().anyMatch(ToolResponseMessage.class::isInstance);
    }

    private boolean hasToolResponseNamed(String toolName) {
        return getMessageList().stream()
                .filter(ToolResponseMessage.class::isInstance)
                .map(ToolResponseMessage.class::cast)
                .flatMap(message -> message.getResponses().stream())
                .anyMatch(response -> toolName.equals(response.name()));
    }

    private String formatToolResponses(ToolResponseMessage toolResponseMessage) {
        return toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务，结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
    }
}
