package com.bvz.aiagent.core.model;

import java.util.List;

public record StepResult(
        List<String> messages,
        List<String> toolCalls,
        ExecutionState nextState,
        boolean terminate
) {

    public StepResult {
        messages = List.copyOf(messages);
        toolCalls = List.copyOf(toolCalls);
        if (nextState == null) {
            throw new IllegalArgumentException("nextState must not be null");
        }
    }
}
