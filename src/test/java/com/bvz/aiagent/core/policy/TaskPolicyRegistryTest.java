package com.bvz.aiagent.core.policy;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.TaskType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskPolicyRegistryTest {

    @Test
    void shouldReturnBaselinePoliciesInRegistrationOrder() {
        TaskPolicy truthfulness = new StubPolicy("truthfulness", true);
        TaskPolicy artifact = new StubPolicy("artifact", true);
        TaskPolicy externalEvidence = new StubPolicy("external-evidence", true);
        TaskPolicy safetyBoundary = new StubPolicy("safety-boundary", true);
        TaskPolicyRegistry registry = new TaskPolicyRegistry(List.of(
                truthfulness,
                artifact,
                externalEvidence,
                safetyBoundary
        ));

        AgentTask task = new AgentTask(
                "task-1",
                "帮我搜索约会地点并生成 PDF",
                "搜索约会地点并生成 PDF",
                TaskType.ARTIFACT,
                "MEDIUM",
                true,
                true,
                java.util.Map.of(),
                java.util.Map.of()
        );

        assertEquals(
                List.of(truthfulness, artifact, externalEvidence, safetyBoundary),
                registry.policiesFor(task)
        );
    }

    @Test
    void shouldFilterPoliciesByTaskSupportWithoutBreakingOrder() {
        TaskPolicy truthfulness = new StubPolicy("truthfulness", true);
        TaskPolicy artifact = new StubPolicy("artifact", false);
        TaskPolicy safetyBoundary = new StubPolicy("safety-boundary", true);
        TaskPolicyRegistry registry = new TaskPolicyRegistry(List.of(truthfulness, artifact, safetyBoundary));

        AgentTask task = new AgentTask("给我一些关系建议");

        assertEquals(List.of(truthfulness, safetyBoundary), registry.policiesFor(task));
    }

    private record StubPolicy(String id, boolean supports) implements TaskPolicy {
        @Override
        public boolean supports(AgentTask task) {
            return supports;
        }

        @Override
        public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
            return new ValidationResult(true, List.of(), false, id);
        }
    }
}
