package com.bvz.aiagent.core.tool;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ToolResultInterpreterRegistry {

    private final Map<String, ToolResultInterpreter<?>> interpreters;

    public ToolResultInterpreterRegistry(List<ToolResultInterpreter<?>> interpreters) {
        this.interpreters = interpreters.stream()
                .collect(Collectors.toMap(ToolResultInterpreter::toolName, Function.identity()));
    }

    public ToolResultInterpreter<?> get(String toolName) {
        ToolResultInterpreter<?> interpreter = interpreters.get(toolName);
        if (interpreter == null) {
            throw new IllegalArgumentException("No interpreter registered for tool: " + toolName);
        }
        return interpreter;
    }
}
