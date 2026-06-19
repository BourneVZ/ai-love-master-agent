package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.skill.SkillRegistry;

public class AgentOrchestrator {

    private final TaskClassifier taskClassifier;
    private final PlanGenerator planGenerator;
    private final ModelStepExecutor executor;
    private final DefaultCompletionValidator validator;
    private final DefaultRepairStrategy repairStrategy;
    private final SkillRegistry skillRegistry;

    public AgentOrchestrator(
            TaskClassifier taskClassifier,
            PlanGenerator planGenerator,
            ModelStepExecutor executor,
            DefaultCompletionValidator validator,
            DefaultRepairStrategy repairStrategy,
            SkillRegistry skillRegistry
    ) {
        this.taskClassifier = taskClassifier;
        this.planGenerator = planGenerator;
        this.executor = executor;
        this.validator = validator;
        this.repairStrategy = repairStrategy;
        this.skillRegistry = skillRegistry;
    }

    public AgentResult run(AgentTask task) {
        ExecutionState state = ExecutionState.initial();
        ExecutionPlan plan = planGenerator.createPlan(task, state);
        SkillRegistry.GuidanceBundle guidanceBundle = skillRegistry.buildGuidance(
                task,
                new SuccessContract(false, false, null, null, true, false, false)
        );

        StepResult stepResult = executor.executeNextStep(task, state, plan);
        ExecutionState nextState = stepResult.nextState();
        ValidationResult validation = validator.validate(task, nextState, guidanceBundle.contract());
        if (!validation.passed() && validation.repairable()) {
            repairStrategy.buildRepairInstruction(validation, nextState);
        }

        return new AgentResult(
                validation.passed() ? AgentResult.Status.COMPLETED : AgentResult.Status.FAILED,
                nextState.observations().isEmpty() ? "" : nextState.observations().getLast(),
                validation.summary(),
                nextState.partialArtifacts()
        );
    }
}
