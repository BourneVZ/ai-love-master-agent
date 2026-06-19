package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;

import java.util.List;

public class ExternalEvidencePolicy implements TaskPolicy {

    @Override
    public boolean supports(AgentTask task) {
        return true;
    }

    @Override
    public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
        if (!contract.requiresExternalEvidence()) {
            return new ValidationResult(true, List.of(), false, "external evidence check skipped");
        }

        boolean hasSearchEvidence = state.toolHistory().stream()
                .anyMatch(entry -> entry.startsWith("searchWeb:SUCCESS") || entry.startsWith("searchImage:SUCCESS"));
        boolean hasExplicitEvidence = state.observations().stream()
                .anyMatch(observation -> observation.startsWith("EVIDENCE:"));

        if (hasSearchEvidence || hasExplicitEvidence) {
            return new ValidationResult(true, List.of(), false, "external evidence validation passed");
        }

        return new ValidationResult(
                false,
                List.of("external evidence missing: no successful external evidence was collected"),
                true,
                "external evidence validation failed"
        );
    }
}
