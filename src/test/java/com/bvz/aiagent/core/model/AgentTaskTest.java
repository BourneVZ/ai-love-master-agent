package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTaskTest {

    @Test
    void shouldAssembleAllTaskFields() {
        AgentTask task = new AgentTask(
                "task-1",
                "帮我规划一次约会",
                "规划一次约会",
                TaskType.ARTIFACT,
                "MEDIUM",
                true,
                true,
                Map.of("chatId", "chat-1"),
                Map.of("tone", "warm")
        );

        assertEquals("task-1", task.taskId());
        assertEquals("帮我规划一次约会", task.originalUserRequest());
        assertEquals("规划一次约会", task.normalizedGoal());
        assertEquals(TaskType.ARTIFACT, task.taskType());
        assertEquals("MEDIUM", task.riskLevel());
        assertTrue(task.artifactRequirement());
        assertTrue(task.externalInfoRequirement());
        assertEquals("chat-1", task.conversationContext().get("chatId"));
        assertEquals("warm", task.userPreferences().get("tone"));
    }

    @Test
    void shouldApplyDefaultsForMinimalTask() {
        AgentTask task = new AgentTask("给我一些沟通建议");

        assertNotNull(task.taskId());
        assertEquals("给我一些沟通建议", task.originalUserRequest());
        assertEquals("给我一些沟通建议", task.normalizedGoal());
        assertEquals(TaskType.HYBRID, task.taskType());
        assertEquals("MEDIUM", task.riskLevel());
        assertFalse(task.artifactRequirement());
        assertFalse(task.externalInfoRequirement());
        assertTrue(task.conversationContext().isEmpty());
        assertTrue(task.userPreferences().isEmpty());
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new AgentTask(null));
        assertThrows(IllegalArgumentException.class, () -> new AgentTask("   "));
        assertThrows(IllegalArgumentException.class, () -> new AgentTask(
                "task-1",
                "原始请求",
                "",
                TaskType.ADVICE,
                "LOW",
                false,
                false,
                Map.of(),
                Map.of()
        ));
    }
}
