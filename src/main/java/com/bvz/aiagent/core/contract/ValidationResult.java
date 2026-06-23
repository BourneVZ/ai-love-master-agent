package com.bvz.aiagent.core.contract;

import java.util.List;

public record ValidationResult(
        boolean passed,
        List<String> issues,
        boolean repairable,
        String summary
) {

    public ValidationResult {
        issues = List.copyOf(issues);
        summary = summary == null ? "" : summary;
        if (!passed && issues.isEmpty()) {
            throw new IllegalArgumentException("issues must not be empty when validation fails");
        }
    }
}
