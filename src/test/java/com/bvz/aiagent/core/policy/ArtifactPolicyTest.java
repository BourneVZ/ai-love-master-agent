package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.ArtifactContract;
import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.TaskType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactPolicyTest {

    private final ArtifactPolicy policy = new ArtifactPolicy();

    @Test
    void shouldFailWhenRequiredArtifactDoesNotExist() {
        AgentTask task = new AgentTask(
                "task-1",
                "生成约会计划 PDF",
                "生成约会计划 PDF",
                TaskType.ARTIFACT,
                "MEDIUM",
                true,
                false,
                Map.of(),
                Map.of()
        );
        ExecutionState state = ExecutionState.initial();
        SuccessContract contract = new SuccessContract(
                false,
                true,
                new ArtifactContract("PDF", true, true, false, List.of("行程"), List.of("pdf")),
                null,
                true,
                false,
                false
        );

        ValidationResult result = policy.validate(task, state, contract);

        assertFalse(result.passed());
        assertTrue(result.repairable());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("artifact")));
    }

    @Test
    void shouldFailWhenForbiddenExtraArtifactsAreProduced() {
        AgentTask task = new AgentTask("生成约会计划 PDF");
        ExecutionState state = new ExecutionState(
                2,
                1,
                0,
                List.of("generatePDF:SUCCESS", "writeFile:SUCCESS"),
                List.of("ARTIFACT_READABLE:/tmp/date-plan.pdf"),
                List.of("/tmp/date-plan.pdf", "/tmp/date-plan.txt"),
                List.of(),
                ExecutionState.Status.COMPLETED
        );
        SuccessContract contract = new SuccessContract(
                false,
                true,
                new ArtifactContract("PDF", true, true, false, List.of(), List.of("pdf")),
                null,
                true,
                false,
                true
        );

        ValidationResult result = policy.validate(task, state, contract);

        assertFalse(result.passed());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("extra")));
    }
}
