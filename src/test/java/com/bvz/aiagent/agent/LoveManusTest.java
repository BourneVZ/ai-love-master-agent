package com.bvz.aiagent.agent;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoveManusTest {

    @Test
    void shouldKeepLoveManusAsFacadeEntryAndDelegateToAdapter() {
        ToolCallAgentAdapter adapter = mock(ToolCallAgentAdapter.class);
        when(adapter.run("generate a date plan pdf")).thenReturn("facade-result");

        LoveManus loveManus = new LoveManus(adapter);

        assertEquals("facade-result", loveManus.run("generate a date plan pdf"));
        verify(adapter).run("generate a date plan pdf");
    }

    @Test
    void shouldKeepStreamingFacadeEntryAvailable() {
        ToolCallAgentAdapter adapter = mock(ToolCallAgentAdapter.class);
        when(adapter.run(eq("stream a date plan"), any())).thenReturn("stream-result");

        LoveManus loveManus = new LoveManus(adapter);
        SseEmitter emitter = loveManus.runStream("stream a date plan");

        assertNotNull(emitter);
        verify(adapter, timeout(1000)).run(eq("stream a date plan"), any());
    }
}
