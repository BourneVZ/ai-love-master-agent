package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;

import java.util.List;

public class SafetyBoundaryPolicy implements TaskPolicy {

    @Override
    public boolean supports(AgentTask task) {
        return true;
    }

    @Override
    public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
        if (state.violations().isEmpty()) {
            return new ValidationResult(true, List.of(), false, "safety boundary validation passed");
        }
        return new ValidationResult(
                false,
                List.copyOf(state.violations()),
                false,
                "safety-boundary validation failed"
        );
    }
}
