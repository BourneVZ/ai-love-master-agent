package com.bvz.aiagent.agent;

import com.bvz.aiagent.core.model.AgentResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.runtime.AgentOrchestrator;

import java.util.function.Consumer;

public class ToolCallAgentAdapter {

    private final AgentOrchestrator orchestrator;

    public ToolCallAgentAdapter(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public String run(String userPrompt) {
        AgentResult result = orchestrator.run(new AgentTask(userPrompt));
        return result.output();
    }

    public String run(String userPrompt, Consumer<String> stepConsumer) {
        AgentResult result = orchestrator.run(new AgentTask(userPrompt), stepConsumer);
        return result.output();
    }
}
