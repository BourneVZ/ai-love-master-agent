package com.bvz.aiagent.domain.love.skills;

import com.bvz.aiagent.core.contract.ArtifactContract;
import com.bvz.aiagent.core.contract.CoverageContract;
import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.skill.Skill;
import com.bvz.aiagent.core.skill.SkillGuidance;

import java.util.List;

public class DatePlanSkill implements Skill {

    @Override
    public String id() {
        return "date-plan";
    }

    @Override
    public boolean matches(AgentTask task) {
        String text = (task.originalUserRequest() + " " + task.normalizedGoal()).toLowerCase();
        return text.contains("约会") || text.contains("七夕") || text.contains("地点") || text.contains("情侣");
    }

    @Override
    public SkillGuidance buildGuidance(AgentTask task) {
        return new SkillGuidance(
                List.of("优先明确地点数量、预算和偏好约束", "确保地点推荐具备足够覆盖并说明选择理由"),
                List.of("searchWeb", "searchImage", "generatePDF"),
                null
        );
    }

    @Override
    public SuccessContract enrichContract(AgentTask task, SuccessContract baseContract) {
        return new SuccessContract(
                true,
                true,
                new ArtifactContract("PDF", true, true, false, List.of("行程", "地点推荐"), List.of("pdf")),
                new CoverageContract("DATE_SPOTS", 3, 0.6, 1),
                baseContract.truthfulnessRequired(),
                baseContract.localAssetRequired(),
                baseContract.forbiddenExtraArtifacts()
        );
    }
}
