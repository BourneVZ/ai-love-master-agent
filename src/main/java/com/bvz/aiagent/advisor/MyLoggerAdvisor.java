package com.bvz.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest logRequest(ChatClientRequest request) {
        log.info("AI Request: {}", request.prompt());
        return request;
    }

    private void logResponse(ChatClientResponse ChatClientResponse) {
        var output = ChatClientResponse.chatResponse().getResult().getOutput();
        log.info("AI Response: {}", output.getText());
        log.info("AI Tool Calls: {}", output.getToolCalls());
    }
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        chatClientRequest = this.logRequest(chatClientRequest);
        ChatClientResponse ChatClientResponse = chain.nextCall(chatClientRequest);
        this.logResponse(ChatClientResponse);
        return ChatClientResponse;
    }
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        chatClientRequest = this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = chain.nextStream(chatClientRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }
}
