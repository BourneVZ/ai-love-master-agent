package com.bvz.aiagent.agent;

import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolCallAgentAdapterTest {

    @Test
    void shouldDelegateOldAgentEntryToNewRuntimeBoundary() {
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        when(orchestrator.run(any())).thenReturn(new AgentResult(
                AgentResult.Status.COMPLETED,
                "已生成约会计划",
                "summary",
                List.of("/tmp/date-plan.pdf")
        ));
        ToolCallAgentAdapter adapter = new ToolCallAgentAdapter(orchestrator);

        String output = adapter.run("帮我生成一份约会计划 PDF");

        assertEquals("已生成约会计划", output);
    }
}
