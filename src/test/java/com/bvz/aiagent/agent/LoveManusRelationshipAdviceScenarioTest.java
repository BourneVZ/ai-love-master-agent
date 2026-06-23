package com.bvz.aiagent.agent;

import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoveManusRelationshipAdviceScenarioTest {

    @Test
    void shouldHandleRelationshipAdviceWithoutHardcodedToolChain() {
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        when(orchestrator.run(any())).thenReturn(new AgentResult(
                AgentResult.Status.COMPLETED,
                "Give calm and structured advice first.",
                "advice completed",
                List.of()
        ));

        LoveManus loveManus = new LoveManus(new ToolCallAgentAdapter(orchestrator));

        String output = loveManus.run("We argued yesterday. Help me think through how to talk to her.");

        assertEquals("Give calm and structured advice first.", output);
    }
}
