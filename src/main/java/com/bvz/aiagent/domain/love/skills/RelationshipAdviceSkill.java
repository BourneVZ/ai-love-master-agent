package com.bvz.aiagent.domain.love.skills;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.skill.Skill;
import com.bvz.aiagent.core.skill.SkillGuidance;

import java.util.List;

public class RelationshipAdviceSkill implements Skill {

    @Override
    public String id() {
        return "relationship-advice";
    }

    @Override
    public boolean matches(AgentTask task) {
        String text = (task.originalUserRequest() + " " + task.normalizedGoal()).toLowerCase();
        return text.contains("关系") || text.contains("沟通") || text.contains("安慰") || text.contains("吵架");
    }

    @Override
    public SkillGuidance buildGuidance(AgentTask task) {
        return new SkillGuidance(
                List.of("先提供共情回应，再给出结构化建议", "明确风险边界，避免操控或越界建议"),
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
