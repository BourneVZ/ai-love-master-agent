package com.bvz.aiagent.agent;

import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoveManusMessageDraftScenarioTest {

    @Test
    void shouldDraftMessageWithoutRequiringExternalToolsByDefault() {
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        when(orchestrator.run(any())).thenReturn(new AgentResult(
                AgentResult.Status.COMPLETED,
                "Hey, I have been thinking about us and want to talk honestly.",
                "message draft completed",
                List.of()
        ));

        LoveManus loveManus = new LoveManus(new ToolCallAgentAdapter(orchestrator));

        String output = loveManus.run("Draft a sincere apology text for my girlfriend.");

        assertEquals("Hey, I have been thinking about us and want to talk honestly.", output);
    }
}
