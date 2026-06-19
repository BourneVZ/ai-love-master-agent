package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentResultTest {

    @Test
    void shouldCarryFinalOutputSummaryAndArtifacts() {
        AgentResult result = new AgentResult(
                AgentResult.Status.COMPLETED,
                "已生成约会计划",
                "计划包含地点、流程和注意事项",
                List.of("date-plan.pdf")
        );

        assertEquals(AgentResult.Status.COMPLETED, result.status());
        assertEquals("已生成约会计划", result.output());
        assertEquals("计划包含地点、流程和注意事项", result.summary());
        assertEquals(List.of("date-plan.pdf"), result.artifacts());
    }

    @Test
    void shouldRejectMissingStatusOrOutput() {
        assertThrows(IllegalArgumentException.class, () -> new AgentResult(null, "output", "summary", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new AgentResult(AgentResult.Status.FAILED, " ", "summary", List.of()));
    }
}
