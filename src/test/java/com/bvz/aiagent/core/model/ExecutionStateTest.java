package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionStateTest {

    @Test
    void shouldExposeDefaultExecutionState() {
        ExecutionState state = ExecutionState.initial();

        assertEquals(0, state.stepIndex());
        assertEquals(0, state.toolCallCount());
        assertEquals(0, state.planningRound());
        assertTrue(state.toolHistory().isEmpty());
        assertTrue(state.observations().isEmpty());
        assertTrue(state.partialArtifacts().isEmpty());
        assertTrue(state.violations().isEmpty());
        assertEquals(ExecutionState.Status.RUNNING, state.status());
    }

    @Test
    void shouldTrackHistoriesAndStatusTransition() {
        ExecutionState state = new ExecutionState(
                2,
                1,
                1,
                List.of("searchWeb"),
                List.of("找到 3 个候选地点"),
                List.of("date-plan.pdf"),
                List.of("missing-image-coverage"),
                ExecutionState.Status.REPAIRING
        );

        assertEquals(2, state.stepIndex());
        assertEquals(1, state.toolCallCount());
        assertEquals(1, state.planningRound());
        assertEquals(List.of("searchWeb"), state.toolHistory());
        assertEquals(List.of("找到 3 个候选地点"), state.observations());
        assertEquals(List.of("date-plan.pdf"), state.partialArtifacts());
        assertEquals(List.of("missing-image-coverage"), state.violations());
        assertEquals(ExecutionState.Status.REPAIRING, state.status());
    }
}
