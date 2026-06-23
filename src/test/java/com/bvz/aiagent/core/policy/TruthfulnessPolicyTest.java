package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TruthfulnessPolicyTest {

    private final TruthfulnessPolicy policy = new TruthfulnessPolicy();

    @Test
    void shouldFailWhenFinalResponseClaimsDownloadedArtifactWithoutSuccessfulToolEvidence() {
        AgentTask task = new AgentTask("帮我下载一张图片并告诉我已经保存好了");
        ExecutionState state = new ExecutionState(
                1,
                0,
                0,
                List.of(),
                List.of("FINAL_RESPONSE:已下载图片并保存到 /tmp/date.png"),
                List.of(),
                List.of(),
                ExecutionState.Status.RUNNING
        );
        SuccessContract contract = new SuccessContract(false, false, null, null, true, false, false);

        ValidationResult result = policy.validate(task, state, contract);

        assertFalse(result.passed());
        assertTrue(result.repairable());
        assertTrue(result.summary().contains("truth"));
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("download")));
    }

    @Test
    void shouldPassWhenClaimedArtifactMatchesSuccessfulExecutionEvidence() {
        AgentTask task = new AgentTask("帮我生成 PDF");
        ExecutionState state = new ExecutionState(
                2,
                1,
                0,
                List.of("generatePDF:SUCCESS"),
                List.of("FINAL_RESPONSE:已生成 PDF 并保存到 /tmp/agent_output.pdf"),
                List.of("/tmp/agent_output.pdf"),
                List.of(),
                ExecutionState.Status.COMPLETED
        );
        SuccessContract contract = new SuccessContract(false, false, null, null, true, false, false);

        ValidationResult result = policy.validate(task, state, contract);

        assertTrue(result.passed());
    }
}
