package com.bvz.aiagent.agent;

import java.util.Objects;

/**
 * Legacy entry kept only as a compatibility wrapper.
 * Phase 7 migrates execution responsibility to ToolCallAgentAdapter.
 */
public class ToolCallAgent {

    private final ToolCallAgentAdapter adapter;

    public ToolCallAgent(ToolCallAgentAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }

    public String run(String userPrompt) {
        return adapter.run(userPrompt);
    }
}
