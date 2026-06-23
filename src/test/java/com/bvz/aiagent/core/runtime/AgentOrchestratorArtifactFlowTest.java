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

class AgentOrchestratorArtifactFlowTest {

    @Test
    void shouldCompleteArtifactTaskWhenExecutorProducesArtifactAndValidationPasses() {
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

        AgentTask task = new AgentTask("生成约会计划 PDF");
        ExecutionPlan plan = new ExecutionPlan("生成约会计划", List.of("生成产物"), 1);
        ExecutionState completedState = new ExecutionState(
                1,
                1,
                1,
                List.of("generatePDF:SUCCESS"),
                List.of("ARTIFACT_READABLE:/tmp/date-plan.pdf"),
                List.of("/tmp/date-plan.pdf"),
                List.of(),
                ExecutionState.Status.COMPLETED
        );

        when(planGenerator.createPlan(any(), any())).thenReturn(plan);
        when(skillRegistry.buildGuidance(any(), any())).thenReturn(new SkillRegistry.GuidanceBundle(List.of(), List.of(), new SuccessContract(false, false, null, null, true, false, false), List.of()));
        when(executor.executeNextStep(any(), any(), any())).thenReturn(new StepResult(List.of("生成 PDF"), List.of("generatePDF"), completedState, false));
        when(validator.validate(any(), any(), any())).thenReturn(new ValidationResult(true, List.of(), false, "passed"));

        AgentResult result = orchestrator.run(task);

        assertEquals(AgentResult.Status.COMPLETED, result.status());
        assertEquals(List.of("/tmp/date-plan.pdf"), result.artifacts());
    }
}
