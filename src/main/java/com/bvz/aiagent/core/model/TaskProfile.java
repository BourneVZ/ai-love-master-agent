package com.bvz.aiagent.core.model;

public record TaskProfile(
        TaskType taskType,
        String riskLevel,
        boolean externalInfoRequired,
        boolean artifactRequired
) {

    public TaskProfile {
        if (taskType == null) {
            throw new IllegalArgumentException("taskType must not be null");
        }
        if (riskLevel == null || riskLevel.isBlank()) {
            throw new IllegalArgumentException("riskLevel must not be blank");
        }
    }
}
