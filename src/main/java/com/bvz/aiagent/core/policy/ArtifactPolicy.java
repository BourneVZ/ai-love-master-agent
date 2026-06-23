package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.ArtifactContract;
import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;

import java.util.ArrayList;
import java.util.List;

public class ArtifactPolicy implements TaskPolicy {

    @Override
    public boolean supports(AgentTask task) {
        return task.artifactRequirement() || task.taskType() == com.bvz.aiagent.core.model.TaskType.ARTIFACT;
    }

    @Override
    public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
        List<String> issues = new ArrayList<>();
        ArtifactContract artifactContract = contract.artifactContract();

        if (contract.requiresArtifact() && state.partialArtifacts().isEmpty()) {
            issues.add("artifact missing: required artifact was not produced");
        }

        if (artifactContract != null && artifactContract.mustBeReadable()) {
            boolean readable = state.observations().stream()
                    .anyMatch(observation -> observation.startsWith("ARTIFACT_READABLE:"));
            if (!state.partialArtifacts().isEmpty() && !readable) {
                issues.add("artifact unreadable: produced artifact is not marked readable");
            }
        }

        if (contract.forbiddenExtraArtifacts() && state.partialArtifacts().size() > 1) {
            issues.add("extra artifact detected: unexpected additional artifacts were produced");
        }

        if (issues.isEmpty()) {
            return new ValidationResult(true, List.of(), false, "artifact validation passed");
        }
        return new ValidationResult(false, issues, true, "artifact validation failed");
    }
}
