package com.bvz.aiagent.core.model;

import java.util.List;

public record AgentResult(
        Status status,
        String output,
        String summary,
        List<String> artifacts
) {

    public AgentResult {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (output == null || output.isBlank()) {
            throw new IllegalArgumentException("output must not be blank");
        }
        summary = summary == null ? "" : summary;
        artifacts = List.copyOf(artifacts);
    }

    public enum Status {
        COMPLETED,
        FAILED,
        TERMINATED
    }
}
