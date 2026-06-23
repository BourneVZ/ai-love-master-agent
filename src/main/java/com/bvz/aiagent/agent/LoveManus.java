package com.bvz.aiagent.agent;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class LoveManus {

    private final ToolCallAgentAdapter adapter;

    public LoveManus(ToolCallAgentAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }

    public String run(String userPrompt) {
        return adapter.run(userPrompt);
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send(SseEmitter.event().data("错误：不能使用空提示词运行代理"));
                    emitter.complete();
                    return;
                }

                List<String> streamedSteps = new ArrayList<>();
                String finalOutput = adapter.run(userPrompt, step -> {
                    streamedSteps.add(step);
                    sendStep(emitter, step);
                });
                if (StrUtil.isNotBlank(finalOutput) && streamedSteps.stream().noneMatch(step -> step.contains(finalOutput))) {
                    sendStep(emitter, finalOutput);
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("执行 LoveManus 流式任务失败", e);
                try {
                    emitter.send(SseEmitter.event().data("执行错误: " + e.getMessage()));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        emitter.onTimeout(() -> log.warn("LoveManus SSE connection timed out"));
        emitter.onCompletion(() -> log.info("LoveManus SSE connection completed"));
        return emitter;
    }

    private void sendStep(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().data(message));
        } catch (IOException e) {
            throw new RuntimeException("发送流式消息失败", e);
        }
    }
}
