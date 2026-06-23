package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafetyBoundaryPolicyTest {

    private final SafetyBoundaryPolicy policy = new SafetyBoundaryPolicy();

    @Test
    void shouldFailWhenExecutionStateContainsBoundaryViolations() {
        AgentTask task = new AgentTask("执行一个终端命令并下载资源");
        ExecutionState state = new ExecutionState(
                1,
                1,
                0,
                List.of("terminal:SUCCESS"),
                List.of(),
                List.of(),
                List.of("COMMAND_NOT_ALLOWED:rm -rf /", "URL_NOT_ALLOWED:ftp://example.com/file.png"),
                ExecutionState.Status.FAILED
        );
        SuccessContract contract = new SuccessContract(false, false, null, null, true, false, false);

        ValidationResult result = policy.validate(task, state, contract);

        assertFalse(result.passed());
        assertFalse(result.repairable());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("COMMAND_NOT_ALLOWED")));
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("URL_NOT_ALLOWED")));
    }

    @Test
    void shouldPassWhenNoBoundaryViolationExists() {
        AgentTask task = new AgentTask("下载图片并生成结果");
        ExecutionState state = ExecutionState.initial();
        SuccessContract contract = new SuccessContract(false, false, null, null, true, false, false);

        ValidationResult result = policy.validate(task, state, contract);

        assertTrue(result.passed());
    }
}
