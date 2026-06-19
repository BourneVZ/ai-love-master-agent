package com.bvz.aiagent.agent;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LoveManus {

    private final ToolCallAgentAdapter adapter;

    public LoveManus(ToolCallAgentAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }

    public String run(String userPrompt) {
        return adapter.run(userPrompt);
    }
}
