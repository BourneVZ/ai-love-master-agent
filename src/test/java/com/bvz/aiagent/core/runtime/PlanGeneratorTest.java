package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.TaskType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanGeneratorTest {

    private final PlanGenerator planGenerator = new PlanGenerator();

    @Test
    void shouldGenerateLightweightPlanFromTaskAndState() {
        AgentTask task = new AgentTask(
                "task-1",
                "帮我生成一份约会计划 PDF",
                "生成约会计划 PDF",
                TaskType.ARTIFACT,
                "MEDIUM",
                true,
                true,
                Map.of(),
                Map.of()
        );
        ExecutionState state = ExecutionState.initial();

        ExecutionPlan plan = planGenerator.createPlan(task, state);

        assertTrue(plan.goalSummary().contains("约会计划"));
        assertFalse(plan.steps().isEmpty());
        assertTrue(plan.planningRound() >= state.planningRound());
    }

    @Test
    void shouldNotHardcodeConcreteToolOrderIntoPlanSteps() {
        AgentTask task = new AgentTask(
                "task-2",
                "搜索约会地点、找图片并生成 PDF",
                "搜索约会地点、找图片并生成 PDF",
                TaskType.ARTIFACT,
                "MEDIUM",
                true,
                true,
                Map.of(),
                Map.of()
        );

        ExecutionPlan plan = planGenerator.createPlan(task, ExecutionState.initial());

        String planText = String.join(" ", plan.steps()).toLowerCase();
        assertFalse(planText.contains("searchweb -> searchimage -> downloadresource -> generatepdf"));
        assertFalse(planText.contains("先调用searchweb再调用searchimage"));
    }
}
