package com.bvz.aiagent.core.tool;

public interface ToolResultInterpreter<T> {

    String toolName();

    Class<T> resultType();

    T interpret(String rawResult);
}
