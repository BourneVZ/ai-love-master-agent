package com.bvz.aiagent.core.tool;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolCapabilityTest {

    @Test
    void shouldCoverCapabilityTypesAndRiskMarkers() {
        EnumSet<ToolCapability> expected = EnumSet.of(
                ToolCapability.SEARCH,
                ToolCapability.WEB_SCRAPING,
                ToolCapability.DOWNLOAD,
                ToolCapability.FILE_OPERATION,
                ToolCapability.TERMINAL_EXECUTION,
                ToolCapability.ARTIFACT_GENERATION,
                ToolCapability.TASK_TERMINATION
        );

        assertEquals(expected, EnumSet.allOf(ToolCapability.class));
        assertTrue(ToolCapability.TERMINAL_EXECUTION.isHighRisk());
        assertTrue(ToolCapability.DOWNLOAD.isHighRisk());
    }
}
