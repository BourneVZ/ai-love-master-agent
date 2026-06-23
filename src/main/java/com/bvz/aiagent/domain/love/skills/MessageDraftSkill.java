package com.bvz.aiagent.domain.love.skills;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.skill.Skill;
import com.bvz.aiagent.core.skill.SkillGuidance;

import java.util.List;

public class MessageDraftSkill implements Skill {

    @Override
    public String id() {
        return "message-draft";
    }

    @Override
    public boolean matches(AgentTask task) {
        String text = (task.originalUserRequest() + " " + task.normalizedGoal()).toLowerCase();
        return text.contains("道歉") || text.contains("表白") || text.contains("文案") || text.contains("脚本");
    }

    @Override
    public SkillGuidance buildGuidance(AgentTask task) {
        return new SkillGuidance(
                List.of("控制语气与长度，保持表达自然具体", "优先输出可直接发送的结构化文本"),
                List.of(),
                null
        );
    }

    @Override
    public SuccessContract enrichContract(AgentTask task, SuccessContract baseContract) {
        return new SuccessContract(
                false,
                false,
                null,
                null,
                baseContract.truthfulnessRequired(),
                false,
                false
        );
    }
}
