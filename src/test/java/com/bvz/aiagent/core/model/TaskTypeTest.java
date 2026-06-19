package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTypeTest {

    @Test
    void shouldCoverAllPlannedTaskTypes() {
        EnumSet<TaskType> expected = EnumSet.of(
                TaskType.CONVERSATION_ONLY,
                TaskType.ADVICE,
                TaskType.RESEARCH,
                TaskType.ARTIFACT,
                TaskType.ACTION,
                TaskType.HYBRID
        );

        assertEquals(expected, EnumSet.allOf(TaskType.class));
        assertTrue(TaskType.valueOf("HYBRID").name().equals("HYBRID"));
    }
}
