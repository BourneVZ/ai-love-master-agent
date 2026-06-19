package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;

public interface TaskPolicy {

    default String id() {
        return getClass().getSimpleName();
    }

    boolean supports(AgentTask task);

    ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract);
}
