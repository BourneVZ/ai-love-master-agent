package com.bvz.aiagent.core.model;

import java.util.List;

public record RepairInstruction(
        String prompt,
        List<String> reasons,
        boolean allowRetry
) {

    public RepairInstruction {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }
        reasons = List.copyOf(reasons);
        if (reasons.isEmpty()) {
            throw new IllegalArgumentException("reasons must not be empty");
        }
    }
}
