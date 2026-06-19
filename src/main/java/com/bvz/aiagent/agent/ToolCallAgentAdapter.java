package com.bvz.aiagent.agent;

import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;

public class ToolCallAgentAdapter {

    private final AgentOrchestrator orchestrator;

    public ToolCallAgentAdapter(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public String run(String userPrompt) {
        AgentResult result = orchestrator.run(new AgentTask(userPrompt));
        return result.output();
    }
}
