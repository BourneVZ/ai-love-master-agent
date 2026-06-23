package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;

import java.util.ArrayList;
import java.util.List;

public class TruthfulnessPolicy implements TaskPolicy {

    @Override
    public boolean supports(AgentTask task) {
        return true;
    }

    @Override
    public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
        if (!contract.truthfulnessRequired()) {
            return new ValidationResult(true, List.of(), false, "truthfulness check skipped");
        }

        List<String> issues = new ArrayList<>();
        boolean claimedDownload = state.observations().stream()
                .anyMatch(observation -> observation.startsWith("FINAL_RESPONSE:")
                        && containsAnyIgnoreCase(observation, "download", "下载"));
        boolean claimedPdf = state.observations().stream()
                .anyMatch(observation -> observation.startsWith("FINAL_RESPONSE:")
                        && containsAnyIgnoreCase(observation, "pdf", "生成"));

        boolean hasDownloadEvidence = state.toolHistory().stream()
                .anyMatch(entry -> entry.startsWith("downloadResource:SUCCESS"));
        boolean hasPdfEvidence = state.toolHistory().stream()
                .anyMatch(entry -> entry.startsWith("generatePDF:SUCCESS"));

        if (claimedDownload && !hasDownloadEvidence) {
            issues.add("truth violation: claimed download without successful download evidence");
        }
        if (claimedPdf && !hasPdfEvidence) {
            issues.add("truth violation: claimed pdf generation without successful generatePDF evidence");
        }

        if (issues.isEmpty()) {
            return new ValidationResult(true, List.of(), false, "truthfulness passed");
        }
        return new ValidationResult(false, issues, true, "truthfulness validation failed");
    }

    private boolean containsAnyIgnoreCase(String text, String... candidates) {
        String lower = text.toLowerCase();
        for (String candidate : candidates) {
            if (lower.contains(candidate.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
