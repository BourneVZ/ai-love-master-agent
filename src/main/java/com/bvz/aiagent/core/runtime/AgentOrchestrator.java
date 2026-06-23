package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.model.TaskProfile;
import com.bvz.aiagent.core.model.TaskType;
import com.bvz.aiagent.core.skill.SkillRegistry;

import java.util.List;
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
        return run(task, null);
    }

    public AgentResult run(AgentTask task, Consumer<String> stepConsumer) {
        TaskProfile profile = taskClassifier.classify(task.originalUserRequest());
        if (profile == null) {
            profile = new TaskProfile(TaskType.HYBRID, "MEDIUM", false, false);
        }
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

        StepResult stepResult = stepConsumer == null
                ? executor.executeNextStep(classifiedTask, state, plan)
                : executor.executeNextStep(classifiedTask, state, plan, stepConsumer);
        if (stepResult == null) {
            ExecutionState failedState = new ExecutionState(
                    state.stepIndex(),
                    state.toolCallCount(),
                    state.planningRound(),
                    state.toolHistory(),
                    List.of("FINAL_RESPONSE: 执行器未返回有效结果"),
                    state.partialArtifacts(),
                    state.violations(),
                    ExecutionState.Status.FAILED
            );
            stepResult = new StepResult(List.of(), List.of(), failedState, true);
        }
        ExecutionState nextState = stepResult.nextState();
        ValidationResult validation = validator.validate(classifiedTask, nextState, guidanceBundle.contract());
        if (!validation.passed() && validation.repairable()) {
            repairStrategy.buildRepairInstruction(validation, nextState);
        }

        return new AgentResult(
                validation.passed() ? AgentResult.Status.COMPLETED : AgentResult.Status.FAILED,
                displayOutput(nextState),
                validation.summary(),
                nextState.partialArtifacts()
        );
    }

    private String displayOutput(ExecutionState state) {
        if (state.observations().isEmpty()) {
            return "任务已执行，但未产出可展示的文本结果";
        }

        String output = state.observations().getLast();
        output = output == null ? "" : output.replaceFirst("^FINAL_RESPONSE:\\s*", "").trim();
        return output.isBlank() ? "任务已执行，但未产出可展示的文本结果" : output;
    }
}
