package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionBudgetTest {

    @Test
    void shouldCarryExecutionLimits() {
        ExecutionBudget budget = new ExecutionBudget(8, 6, 2, 3);

        assertEquals(8, budget.maxSteps());
        assertEquals(6, budget.maxToolCalls());
        assertEquals(2, budget.maxRepairAttempts());
        assertEquals(3, budget.maxPlanningRounds());
    }

    @Test
    void shouldRejectNonPositiveLimits() {
        assertThrows(IllegalArgumentException.class, () -> new ExecutionBudget(0, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionBudget(1, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionBudget(1, 1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionBudget(1, 1, 1, 0));
    }
}
