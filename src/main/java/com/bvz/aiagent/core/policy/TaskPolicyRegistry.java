package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.model.AgentTask;

import java.util.List;

public class TaskPolicyRegistry {

    private final List<TaskPolicy> policies;

    public TaskPolicyRegistry(List<TaskPolicy> policies) {
        this.policies = List.copyOf(policies);
    }

    public List<TaskPolicy> policiesFor(AgentTask task) {
        return policies.stream()
                .filter(policy -> policy.supports(task))
                .toList();
    }
}
