package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalEvidencePolicyTest {

    private final ExternalEvidencePolicy policy = new ExternalEvidencePolicy();

    @Test
    void shouldFailWhenTimeSensitiveTaskHasNoExternalEvidence() {
        AgentTask task = new AgentTask("今天上海有哪些适合约会的活动");
        ExecutionState state = ExecutionState.initial();
        SuccessContract contract = new SuccessContract(true, false, null, null, true, false, false);

        ValidationResult result = policy.validate(task, state, contract);

        assertFalse(result.passed());
        assertTrue(result.repairable());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("external")));
    }

    @Test
    void shouldPassWhenSearchEvidenceExists() {
        AgentTask task = new AgentTask("今天上海有哪些适合约会的活动");
        ExecutionState state = new ExecutionState(
                1,
                1,
                0,
                List.of("searchWeb:SUCCESS"),
                List.of("EVIDENCE:https://example.com/events"),
                List.of(),
                List.of(),
                ExecutionState.Status.RUNNING
        );
        SuccessContract contract = new SuccessContract(true, false, null, null, true, false, false);

        ValidationResult result = policy.validate(task, state, contract);

        assertTrue(result.passed());
    }
}
