package com.bvz.aiagent.core.tool;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolResultInterpreterRegistryTest {

    @Test
    void shouldRegisterAndResolveInterpretersByToolName() {
        ToolResultInterpreter<String> searchInterpreter = stub("searchWeb");
        ToolResultInterpreter<String> pdfInterpreter = stub("generatePDF");
        ToolResultInterpreterRegistry registry = new ToolResultInterpreterRegistry(List.of(searchInterpreter, pdfInterpreter));

        assertSame(searchInterpreter, registry.get("searchWeb"));
        assertSame(pdfInterpreter, registry.get("generatePDF"));
    }

    @Test
    void shouldRejectUnknownToolName() {
        ToolResultInterpreterRegistry registry = new ToolResultInterpreterRegistry(List.of(stub("searchWeb")));

        assertThrows(IllegalArgumentException.class, () -> registry.get("searchImage"));
    }

    private ToolResultInterpreter<String> stub(String toolName) {
        return new ToolResultInterpreter<>() {
            @Override
            public String toolName() {
                return toolName;
            }

            @Override
            public Class<String> resultType() {
                return String.class;
            }

            @Override
            public String interpret(String rawResult) {
                return rawResult;
            }
        };
    }
}
