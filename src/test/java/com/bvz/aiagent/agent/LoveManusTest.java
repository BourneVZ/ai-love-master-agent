package com.bvz.aiagent.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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
}
