package com.bvz.aiagent.core.model;

import java.util.List;

public record ExecutionPlan(
        String goalSummary,
        List<String> steps,
        int planningRound
) {

    public ExecutionPlan {
        if (goalSummary == null || goalSummary.isBlank()) {
            throw new IllegalArgumentException("goalSummary must not be blank");
        }
        steps = List.copyOf(steps);
        if (planningRound < 0) {
            throw new IllegalArgumentException("planningRound must not be negative");
        }
    }
}
