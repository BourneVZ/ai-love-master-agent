package com.bvz.aiagent.tools;

import com.bvz.aiagent.core.tool.ToolDescriptor;
import com.bvz.aiagent.core.tool.ToolResultInterpreterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistrationIntegrationTest {

    @Test
    void shouldRegisterToolCallbacksDescriptorsAndInterpretersCentrally() {
        ToolRegistration registration = new ToolRegistration();

        ToolCallback[] callbacks = registration.allTools();
        Map<String, ToolDescriptor> descriptors = registration.toolDescriptors();
        ToolResultInterpreterRegistry interpreters = registration.toolResultInterpreterRegistry();

        assertNotNull(callbacks);
        assertTrue(callbacks.length > 0);
        assertTrue(descriptors.containsKey("searchWeb"));
        assertTrue(descriptors.containsKey("downloadResource"));
        assertTrue(descriptors.containsKey("generatePDF"));
        assertNotNull(interpreters.get("searchWeb"));
        assertNotNull(interpreters.get("downloadResource"));
        assertNotNull(interpreters.get("generatePDF"));
    }
}
