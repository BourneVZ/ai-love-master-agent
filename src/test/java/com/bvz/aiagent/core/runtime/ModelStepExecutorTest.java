package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.StepResult;
import com.bvz.aiagent.core.tool.ToolResultInterpreterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelStepExecutorTest {

    @Test
    void shouldParseToolCallAndWriteBackStructuredObservation() {
        ModelStepExecutor executor = new ModelStepExecutor(new ToolResultInterpreterRegistry(List.of(
                new com.bvz.aiagent.core.tool.interpreter.WebSearchResultInterpreter()
        )));

        StepResult result = executor.executeNextStep(
                new AgentTask("搜索今天适合约会的地点"),
                ExecutionState.initial(),
                new ExecutionPlan("先找地点信息", List.of("补充外部信息"), 1),
                "CALL searchWeb",
                "searchWeb",
                "{\"title\":\"A\",\"snippet\":\"S1\",\"link\":\"https://example.com/a\"}"
        );

        assertEquals(List.of("searchWeb"), result.toolCalls());
        assertEquals(1, result.nextState().toolCallCount());
        assertTrue(result.nextState().observations().stream().anyMatch(item -> item.contains("https://example.com/a")));
    }

    @Test
    void shouldTerminateWhenModelChoosesTerminateAction() {
        ModelStepExecutor executor = new ModelStepExecutor(new ToolResultInterpreterRegistry(List.of()));

        StepResult result = executor.executeNextStep(
                new AgentTask("任务已完成"),
                ExecutionState.initial(),
                new ExecutionPlan("完成任务", List.of("结束"), 1),
                "TERMINATE",
                null,
                null
        );

        assertTrue(result.terminate());
        assertEquals(ExecutionState.Status.COMPLETED, result.nextState().status());
    }
}
