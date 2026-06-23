package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.policy.TaskPolicy;
import com.bvz.aiagent.core.policy.TaskPolicyRegistry;

import java.util.ArrayList;
import java.util.List;

public class DefaultCompletionValidator {

    private final TaskPolicyRegistry policyRegistry;

    public DefaultCompletionValidator(TaskPolicyRegistry policyRegistry) {
        this.policyRegistry = policyRegistry;
    }

    public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
        List<String> issues = new ArrayList<>();
        List<String> summaries = new ArrayList<>();
        boolean repairable = true;

        for (TaskPolicy policy : policyRegistry.policiesFor(task)) {
            ValidationResult result = policy.validate(task, state, contract);
            if (!result.passed()) {
                issues.addAll(result.issues());
                String policyId = policy.id();
                summaries.add(policyId + ": " + result.summary());
                repairable = repairable && result.repairable();
            }
        }

        if (issues.isEmpty()) {
            return new ValidationResult(true, List.of(), false, "completion validation passed");
        }

        return new ValidationResult(false, issues, repairable, String.join("; ", summaries));
    }
}
