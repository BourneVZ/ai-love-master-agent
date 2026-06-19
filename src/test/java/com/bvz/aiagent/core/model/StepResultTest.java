package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepResultTest {

    @Test
    void shouldCarryMessagesToolCallsStateChangeAndTerminateFlag() {
        ExecutionState nextState = new ExecutionState(
                1,
                1,
                0,
                List.of("searchWeb:SUCCESS"),
                List.of("EVIDENCE:https://example.com"),
                List.of(),
                List.of(),
                ExecutionState.Status.RUNNING
        );
        StepResult result = new StepResult(
                List.of("先搜索地点信息"),
                List.of("searchWeb"),
                nextState,
                false
        );

        assertEquals(List.of("先搜索地点信息"), result.messages());
        assertEquals(List.of("searchWeb"), result.toolCalls());
        assertEquals(nextState, result.nextState());
        assertEquals(false, result.terminate());
    }

    @Test
    void shouldRejectMissingNextState() {
        assertThrows(IllegalArgumentException.class, () -> new StepResult(List.of(), List.of(), null, false));
    }
}
