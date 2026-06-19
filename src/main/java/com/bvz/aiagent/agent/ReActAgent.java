package com.bvz.aiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct（Reasoning and Acting）模式的代理抽象类
 * 实现了“思考 -> 行动”的循环模式，便于学习 Agent 的基础执行流程
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步是否需要行动
     *
     * @return true 表示需要执行 act()，false 表示本轮无需行动
     */
    public abstract boolean think();

    /**
     * 执行 think() 选定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：先思考，再决定是否行动
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成，无需行动";
            }
            return act();
        } catch (Exception e) {
            // 发生异常时将 Agent 置为错误状态，避免继续空转
            setState(AgentState.ERROR);
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
