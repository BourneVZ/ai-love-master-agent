package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.ExecutionPlan;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.TaskType;

import java.util.ArrayList;
import java.util.List;

public class PlanGenerator {

    public ExecutionPlan createPlan(AgentTask task, ExecutionState state) {
        List<String> steps = new ArrayList<>();
        steps.add("确认用户目标与完成约束");

        if (task.externalInfoRequirement()) {
            steps.add("补充完成任务所需的外部信息或证据");
        }
        if (task.taskType() == TaskType.ARTIFACT || task.artifactRequirement()) {
            steps.add("整理内容并生成最终产物");
        } else {
            steps.add("基于当前信息形成可交付结果");
        }

        return new ExecutionPlan(
                "围绕" + task.normalizedGoal() + "推进任务",
                steps,
                state.planningRound() + 1
        );
    }
}
