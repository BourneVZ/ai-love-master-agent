package com.bvz.aiagent.core.model;

import java.util.List;

public record ExecutionState(
        int stepIndex,
        int toolCallCount,
        int planningRound,
        List<String> toolHistory,
        List<String> observations,
        List<String> partialArtifacts,
        List<String> violations,
        Status status
) {

    public ExecutionState {
        if (stepIndex < 0) {
            throw new IllegalArgumentException("stepIndex must not be negative");
        }
        if (toolCallCount < 0) {
            throw new IllegalArgumentException("toolCallCount must not be negative");
        }
        if (planningRound < 0) {
            throw new IllegalArgumentException("planningRound must not be negative");
        }
        toolHistory = List.copyOf(toolHistory);
        observations = List.copyOf(observations);
        partialArtifacts = List.copyOf(partialArtifacts);
        violations = List.copyOf(violations);
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }

    public static ExecutionState initial() {
        return new ExecutionState(
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Status.RUNNING
        );
    }

    public enum Status {
        RUNNING,
        REPAIRING,
        COMPLETED,
        FAILED
    }
}
