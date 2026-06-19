package com.bvz.aiagent.core.model;

import java.util.Map;
import java.util.UUID;

public record AgentTask(
        String taskId,
        String originalUserRequest,
        String normalizedGoal,
        TaskType taskType,
        String riskLevel,
        boolean artifactRequirement,
        boolean externalInfoRequirement,
        Map<String, Object> conversationContext,
        Map<String, Object> userPreferences
) {

    public AgentTask {
        taskId = requireNonBlank(taskId, "taskId");
        originalUserRequest = requireNonBlank(originalUserRequest, "originalUserRequest");
        normalizedGoal = requireNonBlank(normalizedGoal, "normalizedGoal");
        if (taskType == null) {
            throw new IllegalArgumentException("taskType must not be null");
        }
        riskLevel = requireNonBlank(riskLevel, "riskLevel");
        conversationContext = Map.copyOf(conversationContext);
        userPreferences = Map.copyOf(userPreferences);
    }

    public AgentTask(String originalUserRequest) {
        this(
                UUID.randomUUID().toString(),
                originalUserRequest,
                originalUserRequest,
                TaskType.HYBRID,
                "MEDIUM",
                false,
                false,
                Map.of(),
                Map.of()
        );
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
