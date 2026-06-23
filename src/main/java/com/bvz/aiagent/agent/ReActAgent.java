package com.bvz.aiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类。
 *
 * @deprecated LoveManus 已切换到 AgentOrchestrator + AutonomousToolRuntime
 * 的组合式运行时架构，不再作为生产 ReAct 执行基类。
 */
@Deprecated(since = "SDD", forRemoval = false)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();

    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成，无需行动";
            }
            return act();
        } catch (Exception e) {
            setState(AgentState.ERROR);
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
