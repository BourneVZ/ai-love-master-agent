package com.bvz.aiagent.app;

import com.bvz.aiagent.rag.QueryRewriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoveAppTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private QueryRewriter queryRewriter;
    private ToolCallback[] allTools;
    private SyncMcpToolCallbackProvider toolCallbackProvider;
    private VectorStore loveAppVectorStore;
    private LoveApp loveApp;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class, RETURNS_SELF);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        queryRewriter = mock(QueryRewriter.class);
        allTools = new ToolCallback[]{mock(ToolCallback.class)};
        toolCallbackProvider = mock(SyncMcpToolCallbackProvider.class);
        loveAppVectorStore = mock(VectorStore.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        loveApp = new LoveApp(chatClient, queryRewriter, allTools, toolCallbackProvider, loveAppVectorStore);
    }

    @Test
    void shouldPassConversationIdWhenDoingChat() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn("hello");
        when(callResponseSpec.chatResponse()).thenReturn(response);

        String output = loveApp.doChat("hi", "chat-1");

        assertEquals("hello", output);
        verify(requestSpec).user("hi");
        assertConversationIdPropagated("chat-1");
    }

    @Test
    void shouldReturnStructuredReport() {
        LoveApp.LoveReport report = new LoveApp.LoveReport("report-title", List.of("s1", "s2"));
        when(callResponseSpec.entity(LoveApp.LoveReport.class)).thenReturn(report);

        LoveApp.LoveReport output = loveApp.doChatWithReport("help", "chat-2");

        assertEquals("report-title", output.title());
        assertEquals(List.of("s1", "s2"), output.suggestions());
        verify(requestSpec).user("help");
        assertConversationIdPropagated("chat-2");
    }

    @Test
    void shouldRewriteQueryBeforeRagChat() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn("rag-answer");
        when(callResponseSpec.chatResponse()).thenReturn(response);
        when(queryRewriter.doQueryRewrite("original")).thenReturn("rewritten");

        String output = loveApp.doChatWithRag("original", "chat-3");

        assertEquals("rag-answer", output);
        verify(queryRewriter).doQueryRewrite("original");
        verify(requestSpec).user("rewritten");
        assertConversationIdPropagated("chat-3");
    }

    @Test
    void shouldCallToolCallbacksWhenToolsChatIsRequested() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn("tool-answer");
        when(callResponseSpec.chatResponse()).thenReturn(response);

        String output = loveApp.doChatWithTools("tool task", "chat-4");

        assertEquals("tool-answer", output);
        verify(requestSpec).toolCallbacks(allTools);
        assertConversationIdPropagated("chat-4");
    }

    @Test
    void shouldCallMcpProviderCallbacksWhenMcpChatIsRequested() {
        ChatResponse response = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        ToolCallback[] mcpTools = new ToolCallback[]{mock(ToolCallback.class), mock(ToolCallback.class)};
        when(response.getResult().getOutput().getText()).thenReturn("mcp-answer");
        when(callResponseSpec.chatResponse()).thenReturn(response);
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(mcpTools);

        String output = loveApp.doChatWithMcp("mcp task", "chat-5");

        assertEquals("mcp-answer", output);
        verify(toolCallbackProvider).getToolCallbacks();
        verify(requestSpec).toolCallbacks(mcpTools);
        assertConversationIdPropagated("chat-5");
    }

    private void assertConversationIdPropagated(String expectedChatId) {
        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestSpec).advisors(captor.capture());
        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class, RETURNS_SELF);
        captor.getValue().accept(advisorSpec);
        verify(advisorSpec).param(ChatMemory.CONVERSATION_ID, expectedChatId);
    }
}
