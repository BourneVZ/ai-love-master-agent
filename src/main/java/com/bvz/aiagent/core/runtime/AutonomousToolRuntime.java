package com.bvz.aiagent.core.runtime;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import com.bvz.aiagent.advisor.MyLoggerAdvisor;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.tool.ToolResultInterpreterRegistry;
import com.bvz.aiagent.core.tool.model.DownloadResult;
import com.bvz.aiagent.core.tool.model.PdfGenerationResult;
import com.bvz.aiagent.core.tool.model.WebSearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutonomousToolRuntime {

    private static final String SYSTEM_PROMPT = """
            你是 LoveManus，一个会主动完成任务的智能体。
            当用户请求当前网页信息、文件、PDF、下载、图片、地图、终端执行等能力时，优先使用工具，不要靠猜测回答。
            只有在对应工具真正执行成功后，才能声称“已经生成文件 / 已完成搜索 / 已找到图片 / 已下载资源”。
            如果某项能力不可用，必须明确说明原因，不允许假装任务已经完成。
            如果用户要求的是最终 PDF、图片或其他指定产物，不要擅自生成额外的 txt / 清洗文本 / 中间缓存文件，除非用户明确要求。
            如果最终产物里需要呈现网络图片，优先先把图片下载到本地，再在最终产物里引用本地图片，避免在生成 PDF 时临时联网抓图。
            对中文用户请求，优先使用中文总结工具结果。
            """;

    private static final String NEXT_STEP_PROMPT = """
            请主动选择合适的工具并继续执行，除非你真的被阻塞，否则不要停下来征求确认。
            如果任务依赖联网信息、地图、图片、文件或 PDF，必须先调用对应工具，再总结结果。
            如果用户要求“结合网络图片”，先搜索图片资源；如果最终产物是 PDF，优先调用 downloadResource 下载图片到本地，再把本地图片嵌入 PDF，不要只放外链。
            如果用户要求输出 PDF 或其他最终文件，先整理内容，再调用生成工具；只有成功后，才能报告保存路径。
            若 PDF 生成失败，优先直接修正内容并重试 PDF 工具，不要绕路生成多余的 txt 中间文件。
            如果任务已经完成，请调用 terminate 工具结束，而不是继续空转。
            """;

    private static final int MAX_STEPS = 20;

    private final ToolResultInterpreterRegistry interpreterRegistry;
    private final ToolCallback[] availableTools;
    private final ToolCallingManager toolCallingManager;
    private final ChatClient chatClient;

    public AutonomousToolRuntime(
            ToolResultInterpreterRegistry interpreterRegistry,
            ToolCallback[] allTools,
            SyncMcpToolCallbackProvider toolCallbackProvider,
            ChatModel dashscopeChatModel
    ) {
        this.interpreterRegistry = interpreterRegistry;
        this.availableTools = Stream.concat(
                Arrays.stream(allTools),
                Arrays.stream(toolCallbackProvider.getToolCallbacks())
        ).toArray(ToolCallback[]::new);
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
    }

    public StepResult execute(AgentTask task, ExecutionState state, ExecutionPlan plan) {
        return execute(task, state, plan, step -> {
        });
    }

    public StepResult execute(
            AgentTask task,
            ExecutionState state,
            ExecutionPlan plan,
            Consumer<String> stepConsumer
    ) {
        return new RuntimeSession(task, state, plan, stepConsumer).run();
    }

    private final class RuntimeSession {
        private final AgentTask task;
        private final ExecutionPlan plan;
        private final ExecutionState initialState;
        private final Consumer<String> stepConsumer;
        private final List<Message> messages = new ArrayList<>();
        private final List<String> stepNarratives = new ArrayList<>();
        private final List<String> toolHistory;
        private final List<String> observations;
        private final List<String> partialArtifacts;
        private final List<String> toolCalls = new ArrayList<>();
        private ChatResponse lastResponse;
        private DashScopeChatOptions currentChatOptions;
        private ToolResponseMessage pendingToolResponseMessage;
        private String latestAssistantText;
        private int executedSteps;
        private boolean terminated;

        private RuntimeSession(
                AgentTask task,
                ExecutionState state,
                ExecutionPlan plan,
                Consumer<String> stepConsumer
        ) {
            this.task = task;
            this.plan = plan;
            this.initialState = state;
            this.stepConsumer = stepConsumer == null ? step -> {
            } : stepConsumer;
            this.toolHistory = new ArrayList<>(state.toolHistory());
            this.observations = new ArrayList<>(state.observations());
            this.partialArtifacts = new ArrayList<>(state.partialArtifacts());
            this.messages.add(new UserMessage(task.originalUserRequest()));
        }

        private StepResult run() {
            for (int i = 0; i < MAX_STEPS && !terminated; i++) {
                executedSteps = i + 1;
                if (!think()) {
                    break;
                }
                String actResult = act();
                if (StrUtil.isNotBlank(actResult)) {
                    String narrative = "Step " + executedSteps + ": " + actResult;
                    stepNarratives.add(narrative);
                    stepConsumer.accept(narrative);
                }
            }

            if (!terminated && executedSteps >= MAX_STEPS) {
                terminated = true;
                String narrative = "Step " + MAX_STEPS + ": Terminated: Reached max steps (" + MAX_STEPS + ")";
                stepNarratives.add(narrative);
                stepConsumer.accept(narrative);
                observations.add("FINAL_RESPONSE: Reached max steps before confirming completion");
            }

            String finalOutput = determineFinalOutput();
            List<String> mergedObservations = new ArrayList<>(observations);
            if (mergedObservations.stream().noneMatch(item -> item.startsWith("FINAL_RESPONSE:"))) {
                mergedObservations.add("FINAL_RESPONSE: " + finalOutput);
            }

            ExecutionState nextState = new ExecutionState(
                    initialState.stepIndex() + Math.max(executedSteps, 1),
                    initialState.toolCallCount() + toolCalls.size(),
                    initialState.planningRound() + 1,
                    toolHistory,
                    mergedObservations,
                    partialArtifacts,
                    initialState.violations(),
                    terminated ? ExecutionState.Status.COMPLETED : ExecutionState.Status.FAILED
            );
            return new StepResult(List.copyOf(stepNarratives), List.copyOf(toolCalls), nextState, terminated);
        }

        private boolean think() {
            messages.add(new UserMessage(buildStepPrompt()));

            String forcedToolName = determineForcedToolName();
            currentChatOptions = buildChatOptions(forcedToolName);
            Prompt prompt = new Prompt(messages, currentChatOptions);

            try {
                lastResponse = chatClient.prompt(prompt)
                        .system(SYSTEM_PROMPT)
                        .toolCallbacks(availableTools)
                        .call()
                        .chatResponse();

                AssistantMessage assistantMessage = lastResponse.getResult().getOutput();
                latestAssistantText = assistantMessage.getText();
                List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

                if (toolCallList.isEmpty()) {
                    if (StrUtil.isNotBlank(forcedToolName)) {
                        ToolResponseMessage fallbackResponse = tryExecuteForcedTool(forcedToolName, latestAssistantText);
                        if (fallbackResponse != null) {
                            pendingToolResponseMessage = fallbackResponse;
                            return true;
                        }
                    }

                    messages.add(assistantMessage);
                    if (isToolRequiredButMissing()) {
                        observations.add("FINAL_RESPONSE: " + safeText(latestAssistantText, "任务依赖工具，但模型未调用必要工具"));
                        return false;
                    }

                    terminated = true;
                    return false;
                }
                return true;
            } catch (Exception e) {
                latestAssistantText = "处理时遇到错误: " + e.getMessage();
                observations.add("FINAL_RESPONSE: " + latestAssistantText);
                return false;
            }
        }

        private String act() {
            if (pendingToolResponseMessage != null) {
                ToolResponseMessage toolResponseMessage = pendingToolResponseMessage;
                pendingToolResponseMessage = null;
                messages.add(toolResponseMessage);
                recordToolResponses(toolResponseMessage);
                return formatToolResponses(toolResponseMessage);
            }

            if (lastResponse == null || !lastResponse.hasToolCalls()) {
                return safeText(latestAssistantText, "没有工具调用");
            }

            ToolResponseMessage blockedToolResponse = buildBlockedToolResponseIfNeeded();
            if (blockedToolResponse != null) {
                messages.add(blockedToolResponse);
                recordToolResponses(blockedToolResponse);
                return formatToolResponses(blockedToolResponse);
            }

            Prompt prompt = new Prompt(messages, currentChatOptions);
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, lastResponse);
            messages.clear();
            messages.addAll(toolExecutionResult.conversationHistory());

            ToolResponseMessage toolResponseMessage =
                    (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
            recordToolResponses(toolResponseMessage);

            boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(response -> "doTerminate".equals(response.name()));
            if (terminateToolCalled) {
                terminated = true;
            }

            return formatToolResponses(toolResponseMessage);
        }

        private void recordToolResponses(ToolResponseMessage toolResponseMessage) {
            toolResponseMessage.getResponses().forEach(response -> {
                String toolName = response.name();
                String rawResult = String.valueOf(response.responseData());
                toolCalls.add(toolName);
                toolHistory.add(toolName + ":" + determineToolStatus(toolName, rawResult));
                appendStructuredSignals(toolName, rawResult);
            });
        }

        private String determineToolStatus(String toolName, String rawResult) {
            if (("downloadResource".equals(toolName) || "generatePDF".equals(toolName))
                    && StrUtil.isNotBlank(rawResult)
                    && JSONUtil.isTypeJSON(rawResult)) {
                return JSONUtil.parseObj(rawResult).getBool("success", false) ? "SUCCESS" : "FAILURE";
            }
            if (rawResult != null && rawResult.toLowerCase().startsWith("skipped ")) {
                return "SKIPPED";
            }
            return "SUCCESS";
        }

        private void appendStructuredSignals(String toolName, String rawResult) {
            if ("searchWeb".equals(toolName)) {
                WebSearchResult result = (WebSearchResult) interpreterRegistry.get(toolName).interpret(rawResult);
                observations.add("EVIDENCE:" + result);
                return;
            }
            if ("searchImage".equals(toolName)) {
                observations.add("EVIDENCE:" + rawResult);
                return;
            }
            if ("downloadResource".equals(toolName)) {
                DownloadResult result = (DownloadResult) interpreterRegistry.get(toolName).interpret(rawResult);
                if (result.success() && StrUtil.isNotBlank(result.localPath())) {
                    observations.add("LOCAL_ASSET:" + result.localPath());
                }
                observations.add(result.toString());
                return;
            }
            if ("generatePDF".equals(toolName)) {
                PdfGenerationResult result = (PdfGenerationResult) interpreterRegistry.get(toolName).interpret(rawResult);
                if (result.success() && StrUtil.isNotBlank(result.localPath())) {
                    partialArtifacts.add(result.localPath());
                }
                if (result.readable() && StrUtil.isNotBlank(result.localPath())) {
                    observations.add("ARTIFACT_READABLE:" + result.localPath());
                }
                observations.add(result.toString());
                return;
            }
            observations.add(rawResult);
        }

        private String determineFinalOutput() {
            if (!partialArtifacts.isEmpty()) {
                return "我已经完成任务，并生成了可读取的 PDF 文件：" + partialArtifacts.getLast();
            }
            if (StrUtil.containsAnyIgnoreCase(task.originalUserRequest(), "pdf") && hasFailedToolResponseNamed("generatePDF")) {
                return "我已经完成了前面的信息检索和资源准备，但 PDF 生成暂时没有成功，因此这次没有产出可用的 PDF 文件。当前失败点在后端 PDF 生成配置，系统已停止重复执行同一个失败步骤；修复配置后可以重新发起生成。";
            }
            if (StrUtil.isNotBlank(latestAssistantText) && !looksLikeFakeCompletion(latestAssistantText)) {
                return latestAssistantText;
            }
            if (!stepNarratives.isEmpty()) {
                return "我已经执行了任务步骤，但还没有拿到可展示的最终产物。请根据上面的步骤结果确认是否需要继续。";
            }
            if (!observations.isEmpty()) {
                return stripObservationPrefix(observations.getLast());
            }
            return "任务已执行，但未产出可展示的文本结果";
        }

        private String buildStepPrompt() {
            return "用户目标：" + task.normalizedGoal() + "\n计划：" + String.join("；", plan.steps()) + "\n" + NEXT_STEP_PROMPT;
        }

        private DashScopeChatOptions buildChatOptions(String forcedToolName) {
            DashScopeChatOptions.DashScopeChatOptionsBuilder builder = DashScopeChatOptions.builder()
                    .parallelToolCalls(false)
                    .toolCallbacks(Arrays.asList(availableTools))
                    .internalToolExecutionEnabled(false);

            if (StrUtil.isNotBlank(forcedToolName)) {
                builder.toolName(forcedToolName);
                builder.toolChoice(DashScopeApiSpec.ChatCompletionRequestParameter.ToolChoiceBuilder.function(forcedToolName));
            }
            return builder.build();
        }

        private String determineForcedToolName() {
            String request = task.originalUserRequest();
            if (StrUtil.isBlank(request)) {
                return null;
            }

            boolean needsSearch = StrUtil.containsAnyIgnoreCase(request,
                    "搜索", "查找", "地点", "约会", "web", "search");
            boolean needsImage = StrUtil.containsAnyIgnoreCase(request,
                    "图片", "image", "照片");
            boolean needsPdf = StrUtil.containsAnyIgnoreCase(request, "pdf");
            boolean needsTextFile = userExplicitlyRequestsTextArtifact(request);

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
                String response = toolCallback.call(arguments);
                return ToolResponseMessage.builder()
                        .responses(List.of(new ToolResponseMessage.ToolResponse(
                                UUID.randomUUID().toString(),
                                toolName,
                                response
                        )))
                        .build();
            } catch (Exception e) {
                observations.add("FINAL_RESPONSE: 工具兜底执行失败: " + e.getMessage());
                return null;
            }
        }

        private String buildFallbackArguments(String toolName, String assistantText) {
            return switch (toolName) {
                case "searchWeb", "searchImage" ->
                        JSONUtil.createObj().set("query", task.originalUserRequest()).toString();
                case "downloadResource" -> buildDownloadFallbackArguments();
                case "generatePDF" ->
                        JSONUtil.createObj()
                                .set("fileName", "agent_output.pdf")
                                .set("content", buildArtifactContent(assistantText))
                                .toString();
                case "writeFile" ->
                        JSONUtil.createObj()
                                .set("fileName", "agent_output.txt")
                                .set("content", buildArtifactContent(assistantText))
                                .toString();
                default -> null;
            };
        }

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

        private String buildArtifactContent(String assistantText) {
            if (StrUtil.isNotBlank(assistantText) && !looksLikeFakeCompletion(assistantText)) {
                return assistantText;
            }

            String toolSummary = messages.stream()
                    .filter(ToolResponseMessage.class::isInstance)
                    .map(ToolResponseMessage.class::cast)
                    .flatMap(message -> message.getResponses().stream())
                    .map(response -> "【" + response.name() + "】\n" + response.responseData())
                    .collect(Collectors.joining("\n\n"));

            return "用户需求：\n" + task.originalUserRequest() + "\n\n工具执行结果汇总：\n" + toolSummary;
        }

        private ToolResponseMessage buildBlockedToolResponseIfNeeded() {
            AssistantMessage assistantMessage = lastResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            if (CollUtil.isEmpty(toolCallList)) {
                return null;
            }

            AssistantMessage.ToolCall firstToolCall = toolCallList.get(0);
            if ("writeFile".equals(firstToolCall.name()) && !userExplicitlyRequestsTextArtifact(task.originalUserRequest())) {
                return ToolResponseMessage.builder()
                        .responses(List.of(new ToolResponseMessage.ToolResponse(
                                UUID.randomUUID().toString(),
                                "writeFile",
                                "Skipped writeFile: user did not explicitly request a text artifact. Continue with the required final artifact instead."
                        )))
                        .build();
            }

            if ("generatePDF".equals(firstToolCall.name())
                    && hasFailedToolResponseNamed("generatePDF")) {
                return ToolResponseMessage.builder()
                        .responses(List.of(new ToolResponseMessage.ToolResponse(
                                UUID.randomUUID().toString(),
                                "generatePDF",
                                "Skipped generatePDF retry: previous PDF generation failed. Do not retry the same failing tool call; summarize the current state to the user without raw exception text."
                        )))
                        .build();
            }

            if ("generatePDF".equals(firstToolCall.name())
                    && requestNeedsPdfWithImages(task.originalUserRequest())
                    && hasToolResponseNamed("searchImage")
                    && !hasToolResponseNamed("downloadResource")) {
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
            return messages.stream().anyMatch(ToolResponseMessage.class::isInstance);
        }

        private boolean hasToolResponseNamed(String toolName) {
            return messages.stream()
                    .filter(ToolResponseMessage.class::isInstance)
                    .map(ToolResponseMessage.class::cast)
                    .flatMap(message -> message.getResponses().stream())
                    .anyMatch(response -> toolName.equals(response.name()));
        }

        private boolean hasFailedToolResponseNamed(String toolName) {
            return messages.stream()
                    .filter(ToolResponseMessage.class::isInstance)
                    .map(ToolResponseMessage.class::cast)
                    .flatMap(message -> message.getResponses().stream())
                    .filter(response -> toolName.equals(response.name()))
                    .map(ToolResponseMessage.ToolResponse::responseData)
                    .map(String::valueOf)
                    .anyMatch(rawResult -> "FAILURE".equals(determineToolStatus(toolName, rawResult)));
        }

        private String findFirstImageUrlFromToolResponses() {
            return messages.stream()
                    .filter(ToolResponseMessage.class::isInstance)
                    .map(ToolResponseMessage.class::cast)
                    .flatMap(message -> message.getResponses().stream())
                    .filter(response -> "searchImage".equals(response.name()))
                    .map(ToolResponseMessage.ToolResponse::responseData)
                    .map(Object::toString)
                    .map(AutonomousToolRuntime.this::extractFirstHttpUrl)
                    .filter(StrUtil::isNotBlank)
                    .findFirst()
                    .orElse(null);
        }

        private boolean isToolRequiredButMissing() {
            if (hasAnyToolResponse()) {
                return false;
            }
            String request = task.originalUserRequest();
            return StrUtil.containsAnyIgnoreCase(request,
                    "pdf", "file", "search", "web", "scrape", "download", "image", "map", "terminal",
                    "PDF", "文件", "搜索", "联网", "网络", "网页", "爬取", "下载", "图片", "地图", "终端", "执行");
        }

        private String formatToolResponses(ToolResponseMessage toolResponseMessage) {
            return toolResponseMessage.getResponses().stream()
                    .map(response -> formatToolResponse(response.name(), String.valueOf(response.responseData())))
                    .collect(Collectors.joining("\n"));
        }

        private String formatToolResponse(String toolName, String rawResult) {
            if ("searchWeb".equals(toolName)) {
                int count = countResultItems(rawResult);
                return "工具 searchWeb 已完成：检索约会地点与参考信息" + formatCount(count) + "。";
            }
            if ("searchImage".equals(toolName)) {
                int count = countResultItems(rawResult);
                return "工具 searchImage 已完成：搜索可用于计划展示的网络图片" + formatCount(count) + "。";
            }
            if ("downloadResource".equals(toolName)) {
                DownloadResult result = (DownloadResult) interpreterRegistry.get(toolName).interpret(rawResult);
                if (result.success()) {
                    return "工具 downloadResource 已完成：下载图片资源，用于生成最终文件。";
                }
                return "工具 downloadResource 执行失败：图片资源未能保存到本地。";
            }
            if ("generatePDF".equals(toolName)) {
                if (rawResult != null && rawResult.toLowerCase().startsWith("skipped ")) {
                    return "工具 generatePDF 已跳过：上一次 PDF 生成未成功，避免重复执行同一个失败步骤。";
                }
                PdfGenerationResult result = (PdfGenerationResult) interpreterRegistry.get(toolName).interpret(rawResult);
                if (result.success() && result.readable()) {
                    return "工具 generatePDF 已完成：生成可读取的 PDF，已保存到 " + result.localPath() + "。";
                }
                return "工具 generatePDF 未能完成：PDF 暂时没有生成，已记录失败原因并停止暴露底层异常。";
            }
            if ("writeFile".equals(toolName) && rawResult != null && rawResult.toLowerCase().startsWith("skipped ")) {
                return "工具 writeFile 已跳过：用户未要求文本文件，继续生成最终产物。";
            }
            if ("doTerminate".equals(toolName)) {
                return "工具 doTerminate 已完成：结束本次智能体任务。";
            }
            return "工具 " + toolName + " 已完成：执行当前步骤任务。";
        }

        private String formatCount(int count) {
            return count > 0 ? "，获得 " + count + " 条结果" : "";
        }

        private int countResultItems(String rawResult) {
            if (StrUtil.isBlank(rawResult)) {
                return 0;
            }
            try {
                if (rawResult.trim().startsWith("[")) {
                    return JSONUtil.parseArray(rawResult).size();
                }
                if (JSONUtil.isTypeJSON(rawResult)) {
                    Object items = JSONUtil.parseObj(rawResult).get("items");
                    if (items instanceof cn.hutool.json.JSONArray array) {
                        return array.size();
                    }
                    return 1;
                }
                return JSONUtil.parseArray("[" + rawResult + "]").size();
            } catch (Exception ignored) {
                return 0;
            }
        }

        private String extractToolError(String rawResult, String fallback) {
            if (StrUtil.isNotBlank(rawResult) && JSONUtil.isTypeJSON(rawResult)) {
                String error = JSONUtil.parseObj(rawResult).getStr("error", "");
                if (StrUtil.isNotBlank(error)) {
                    return error;
                }
            }
            return fallback;
        }

        private String stripObservationPrefix(String observation) {
            if (StrUtil.isBlank(observation)) {
                return "";
            }
            return observation.replaceFirst("^FINAL_RESPONSE:\\s*", "");
        }
    }

    private String extractFirstHttpUrl(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        Matcher matcher = Pattern.compile("https?://[^,\\s\"\\]}]+").matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private boolean requestNeedsPdfWithImages(String request) {
        return StrUtil.isNotBlank(request)
                && StrUtil.containsAnyIgnoreCase(request, "pdf")
                && StrUtil.containsAnyIgnoreCase(request, "图片", "image", "照片");
    }

    private boolean userExplicitlyRequestsTextArtifact(String request) {
        if (StrUtil.isBlank(request)) {
            return false;
        }
        return StrUtil.containsAnyIgnoreCase(request,
                "txt", "text", "markdown", ".md", ".txt", "plain text")
                || StrUtil.containsAny(request, "文本", "txt文件", "文本文件", "纯文本", "Markdown");
    }

    private boolean looksLikeFakeCompletion(String text) {
        return StrUtil.containsAnyIgnoreCase(text,
                "successfully created", "successfully generated", "saved as a pdf", "found of these locations");
    }

    private String safeText(String text, String fallback) {
        return StrUtil.isNotBlank(text) ? text : fallback;
    }
}
