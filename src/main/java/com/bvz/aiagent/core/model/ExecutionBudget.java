package com.bvz.aiagent.core.model;

public record ExecutionBudget(
        int maxSteps,
        int maxToolCalls,
        int maxRepairAttempts,
        int maxPlanningRounds
) {

    public ExecutionBudget {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be positive");
        }
        if (maxToolCalls <= 0) {
            throw new IllegalArgumentException("maxToolCalls must be positive");
        }
        if (maxRepairAttempts < 0) {
            throw new IllegalArgumentException("maxRepairAttempts must not be negative");
        }
        if (maxPlanningRounds <= 0) {
            throw new IllegalArgumentException("maxPlanningRounds must be positive");
        }
    }
}
