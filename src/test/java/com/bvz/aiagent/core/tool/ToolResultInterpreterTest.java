package com.bvz.aiagent.core.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolResultInterpreterTest {

    @Test
    void shouldExposeToolNameTargetTypeAndInterpretationContract() {
        ToolResultInterpreter<String> interpreter = new ToolResultInterpreter<>() {
            @Override
            public String toolName() {
                return "searchWeb";
            }

            @Override
            public Class<String> resultType() {
                return String.class;
            }

            @Override
            public String interpret(String rawResult) {
                return rawResult.trim();
            }
        };

        assertEquals("searchWeb", interpreter.toolName());
        assertEquals(String.class, interpreter.resultType());
        assertEquals("ok", interpreter.interpret(" ok "));
    }
}
