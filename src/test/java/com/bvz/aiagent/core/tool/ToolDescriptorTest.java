package com.bvz.aiagent.core.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolDescriptorTest {

    @Test
    void shouldCarryToolNameCapabilitySideEffectSchemasAndArtifactFlag() {
        ToolDescriptor descriptor = new ToolDescriptor(
                "generatePDF",
                ToolCapability.ARTIFACT_GENERATION,
                ToolDescriptor.SideEffectLevel.HIGH,
                "input-schema",
                "output-schema",
                true,
                true
        );

        assertEquals("generatePDF", descriptor.name());
        assertEquals(ToolCapability.ARTIFACT_GENERATION, descriptor.capabilityType());
        assertEquals(ToolDescriptor.SideEffectLevel.HIGH, descriptor.sideEffectLevel());
        assertEquals("input-schema", descriptor.inputSchema());
        assertEquals("output-schema", descriptor.outputSchema());
        assertTrue(descriptor.supportsRetry());
        assertTrue(descriptor.artifactProducing());
    }

    @Test
    void shouldRejectBlankNameOrMissingCapability() {
        assertThrows(IllegalArgumentException.class, () -> new ToolDescriptor(
                " ",
                ToolCapability.SEARCH,
                ToolDescriptor.SideEffectLevel.LOW,
                "input",
                "output",
                false,
                false
        ));
        assertThrows(IllegalArgumentException.class, () -> new ToolDescriptor(
                "searchWeb",
                null,
                ToolDescriptor.SideEffectLevel.LOW,
                "input",
                "output",
                false,
                false
        ));
    }
}
