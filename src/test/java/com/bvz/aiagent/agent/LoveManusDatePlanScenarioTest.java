package com.bvz.aiagent.agent;

import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoveManusDatePlanScenarioTest {

    @Test
    void shouldCompleteDatePlanScenarioViaNewRuntime() {
        AgentOrchestrator orchestrator = mock(AgentOrchestrator.class);
        when(orchestrator.run(any())).thenReturn(new AgentResult(
                AgentResult.Status.COMPLETED,
                "DATE_PLAN_READY",
                "date plan completed",
                List.of("/tmp/date-plan.pdf")
        ));

        LoveManus loveManus = new LoveManus(new ToolCallAgentAdapter(orchestrator));

        String output = loveManus.run("Plan a date, include images, and generate a PDF.");

        assertEquals("DATE_PLAN_READY", output);
    }
}
