package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskProfileTest {

    @Test
    void shouldCarryTaskTypeRiskExternalInfoAndArtifactRequirements() {
        TaskProfile profile = new TaskProfile(
                TaskType.ARTIFACT,
                "HIGH",
                true,
                true
        );

        assertEquals(TaskType.ARTIFACT, profile.taskType());
        assertEquals("HIGH", profile.riskLevel());
        assertTrue(profile.externalInfoRequired());
        assertTrue(profile.artifactRequired());
    }

    @Test
    void shouldRejectMissingTaskTypeOrBlankRiskLevel() {
        assertThrows(IllegalArgumentException.class, () -> new TaskProfile(null, "MEDIUM", false, false));
        assertThrows(IllegalArgumentException.class, () -> new TaskProfile(TaskType.ADVICE, " ", false, false));
    }
}
