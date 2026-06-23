package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.RepairInstruction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRepairStrategyTest {

    @Test
    void shouldBuildRepairInstructionForRepairableFailuresWithoutMakingBusinessDecision() {
        DefaultRepairStrategy strategy = new DefaultRepairStrategy();
        ValidationResult validation = new ValidationResult(
                false,
                List.of("缺少外部证据", "PDF 不可读"),
                true,
                "可以通过补充证据和重新生成修复"
        );

        RepairInstruction instruction = strategy.buildRepairInstruction(validation, ExecutionState.initial());

        assertTrue(instruction.allowRetry());
        assertTrue(instruction.prompt().contains("缺少外部证据"));
        assertEquals(List.of("缺少外部证据", "PDF 不可读"), instruction.reasons());
    }

    @Test
    void shouldDisallowRetryForNonRepairableFailures() {
        DefaultRepairStrategy strategy = new DefaultRepairStrategy();
        ValidationResult validation = new ValidationResult(
                false,
                List.of("COMMAND_NOT_ALLOWED"),
                false,
                "安全边界阻止继续执行"
        );

        RepairInstruction instruction = strategy.buildRepairInstruction(validation, ExecutionState.initial());

        assertFalse(instruction.allowRetry());
    }
}
