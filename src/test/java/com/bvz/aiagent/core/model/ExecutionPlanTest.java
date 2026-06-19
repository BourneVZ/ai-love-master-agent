package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionPlanTest {

    @Test
    void shouldCarryLightweightStepsGoalSummaryAndPlanningRound() {
        ExecutionPlan plan = new ExecutionPlan(
                "先确认目标，再补足信息，最后输出结果",
                List.of("确认约束", "补充外部信息", "整理输出"),
                1
        );

        assertEquals("先确认目标，再补足信息，最后输出结果", plan.goalSummary());
        assertEquals(List.of("确认约束", "补充外部信息", "整理输出"), plan.steps());
        assertEquals(1, plan.planningRound());
    }

    @Test
    void shouldRejectBlankGoalSummaryOrNegativePlanningRound() {
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(" ", List.of("step"), 0));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan("summary", List.of("step"), -1));
    }
}
