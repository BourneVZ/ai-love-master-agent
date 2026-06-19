package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.skill.SkillRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentOrchestratorAdviceFlowTest {

    @Test
    void shouldNaturallyFinishAdviceTaskWithoutToolChain() {
        TaskClassifier classifier = mock(TaskClassifier.class);
        PlanGenerator planGenerator = mock(PlanGenerator.class);
        ModelStepExecutor executor = mock(ModelStepExecutor.class);
        DefaultCompletionValidator validator = mock(DefaultCompletionValidator.class);
        DefaultRepairStrategy repairStrategy = mock(DefaultRepairStrategy.class);
        SkillRegistry skillRegistry = mock(SkillRegistry.class);
        AgentOrchestrator orchestrator = new AgentOrchestrator(
                classifier,
                planGenerator,
                executor,
                validator,
                repairStrategy,
                skillRegistry
        );

        AgentTask task = new AgentTask("给我一些沟通建议");
        ExecutionPlan plan = new ExecutionPlan("形成建议", List.of("给出结构化建议"), 1);
        ExecutionState completedState = new ExecutionState(
                1,
                0,
                1,
                List.of(),
                List.of("给出三条沟通建议"),
                List.of(),
                List.of(),
                ExecutionState.Status.COMPLETED
        );

        when(planGenerator.createPlan(any(), any())).thenReturn(plan);
        when(skillRegistry.buildGuidance(any(), any())).thenReturn(new SkillRegistry.GuidanceBundle(List.of(), List.of(), new SuccessContract(false, false, null, null, true, false, false), List.of()));
        when(executor.executeNextStep(any(), any(), any())).thenReturn(new StepResult(List.of("建议内容"), List.of(), completedState, true));
        when(validator.validate(any(), any(), any())).thenReturn(new ValidationResult(true, List.of(), false, "passed"));

        AgentResult result = orchestrator.run(task);

        assertEquals(AgentResult.Status.COMPLETED, result.status());
        assertEquals(List.of(), result.artifacts());
    }
}
