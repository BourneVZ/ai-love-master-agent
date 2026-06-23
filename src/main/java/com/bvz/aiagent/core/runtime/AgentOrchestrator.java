package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.model.TaskProfile;
import com.bvz.aiagent.core.skill.SkillRegistry;

import java.util.function.Consumer;

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
        return run(task, step -> {
        });
    }

    public AgentResult run(AgentTask task, Consumer<String> stepConsumer) {
        TaskProfile profile = taskClassifier.classify(task.originalUserRequest());
        AgentTask classifiedTask = new AgentTask(
                task.taskId(),
                task.originalUserRequest(),
                task.normalizedGoal(),
                profile.taskType(),
                profile.riskLevel(),
                profile.artifactRequired(),
                profile.externalInfoRequired(),
                task.conversationContext(),
                task.userPreferences()
        );
        ExecutionState state = ExecutionState.initial();
        ExecutionPlan plan = planGenerator.createPlan(classifiedTask, state);
        SkillRegistry.GuidanceBundle guidanceBundle = skillRegistry.buildGuidance(
                classifiedTask,
                new SuccessContract(false, false, null, null, true, false, false)
        );

        StepResult stepResult = executor.executeNextStep(classifiedTask, state, plan, stepConsumer);
        ExecutionState nextState = stepResult.nextState();
        ValidationResult validation = validator.validate(classifiedTask, nextState, guidanceBundle.contract());
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
