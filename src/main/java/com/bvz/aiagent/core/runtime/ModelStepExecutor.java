package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.tool.ToolResultInterpreterRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModelStepExecutor {

    private final ToolResultInterpreterRegistry interpreterRegistry;
    private final AutonomousToolRuntime autonomousToolRuntime;

    public ModelStepExecutor(ToolResultInterpreterRegistry interpreterRegistry) {
        this(interpreterRegistry, null);
    }

    public ModelStepExecutor(
            ToolResultInterpreterRegistry interpreterRegistry,
            AutonomousToolRuntime autonomousToolRuntime
    ) {
        this.interpreterRegistry = interpreterRegistry;
        this.autonomousToolRuntime = autonomousToolRuntime;
    }

    public StepResult executeNextStep(
            AgentTask task,
            ExecutionState state,
            ExecutionPlan plan,
            String modelDecision,
            String toolName,
            String rawToolResult
    ) {
        if ("TERMINATE".equalsIgnoreCase(modelDecision)) {
            ExecutionState nextState = new ExecutionState(
                    state.stepIndex() + 1,
                    state.toolCallCount(),
                    state.planningRound(),
                    state.toolHistory(),
                    state.observations(),
                    state.partialArtifacts(),
                    state.violations(),
                    ExecutionState.Status.COMPLETED
            );
            return new StepResult(List.of("Task terminated"), List.of(), nextState, true);
        }

        List<String> toolHistory = new ArrayList<>(state.toolHistory());
        List<String> observations = new ArrayList<>(state.observations());
        List<String> toolCalls = new ArrayList<>();

        if (toolName != null && !toolName.isBlank()) {
            toolCalls.add(toolName);
            toolHistory.add(toolName + ":SUCCESS");
            Object interpreted = interpreterRegistry.get(toolName).interpret(rawToolResult);
            observations.add(interpreted.toString());
        }

        ExecutionState nextState = new ExecutionState(
                state.stepIndex() + 1,
                state.toolCallCount() + toolCalls.size(),
                state.planningRound(),
                toolHistory,
                observations,
                state.partialArtifacts(),
                state.violations(),
                ExecutionState.Status.RUNNING
        );

        return new StepResult(List.of(modelDecision), toolCalls, nextState, false);
    }

    public StepResult executeNextStep(AgentTask task, ExecutionState state, ExecutionPlan plan) {
        return executeNextStep(task, state, plan, step -> {
        });
    }

    public StepResult executeNextStep(
            AgentTask task,
            ExecutionState state,
            ExecutionPlan plan,
            Consumer<String> stepConsumer
    ) {
        if (autonomousToolRuntime == null) {
            ExecutionState nextState = new ExecutionState(
                    state.stepIndex() + 1,
                    state.toolCallCount(),
                    state.planningRound(),
                    state.toolHistory(),
                    List.of("FINAL_RESPONSE: executor runtime is not configured"),
                    state.partialArtifacts(),
                    state.violations(),
                    ExecutionState.Status.FAILED
            );
            return new StepResult(List.of("runtime not configured"), List.of(), nextState, true);
        }
        return autonomousToolRuntime.execute(task, state, plan, stepConsumer);
    }
}
