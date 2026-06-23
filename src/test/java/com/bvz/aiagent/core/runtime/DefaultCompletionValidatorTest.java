package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.policy.TaskPolicy;
import com.bvz.aiagent.core.policy.TaskPolicyRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCompletionValidatorTest {

    @Test
    void shouldAggregatePolicyFailuresAndRemainRepairableWhenAllFailuresAreRepairable() {
        TaskPolicyRegistry registry = new TaskPolicyRegistry(List.of(
                new StubPolicy("truthfulness", new ValidationResult(true, List.of(), false, "ok")),
                new StubPolicy("artifact", new ValidationResult(false, List.of("artifact missing"), true, "repair artifact"))
        ));
        DefaultCompletionValidator validator = new DefaultCompletionValidator(registry);

        ValidationResult result = validator.validate(
                new AgentTask("生成 PDF"),
                ExecutionState.initial(),
                new SuccessContract(false, false, null, null, true, false, false)
        );

        assertFalse(result.passed());
        assertTrue(result.repairable());
        assertEquals(List.of("artifact missing"), result.issues());
        assertTrue(result.summary().contains("artifact"));
    }

    @Test
    void shouldMarkValidationAsNonRepairableWhenAnyPolicyFailureIsNonRepairable() {
        TaskPolicyRegistry registry = new TaskPolicyRegistry(List.of(
                new StubPolicy("truthfulness", new ValidationResult(false, List.of("fake completion"), true, "retry needed")),
                new StubPolicy("safety-boundary", new ValidationResult(false, List.of("COMMAND_NOT_ALLOWED"), false, "unsafe")))
        );
        DefaultCompletionValidator validator = new DefaultCompletionValidator(registry);

        ValidationResult result = validator.validate(
                new AgentTask("执行终端命令"),
                ExecutionState.initial(),
                new SuccessContract(false, false, null, null, true, false, false)
        );

        assertFalse(result.passed());
        assertFalse(result.repairable());
        assertEquals(List.of("fake completion", "COMMAND_NOT_ALLOWED"), result.issues());
        assertTrue(result.summary().contains("safety-boundary"));
    }

    private record StubPolicy(String id, ValidationResult result) implements TaskPolicy {
        @Override
        public boolean supports(AgentTask task) {
            return true;
        }

        @Override
        public ValidationResult validate(AgentTask task, ExecutionState state, SuccessContract contract) {
            return result;
        }
    }
}
