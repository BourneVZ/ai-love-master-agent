package com.bvz.aiagent.core.skill;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;

public interface Skill {

    String id();

    boolean matches(AgentTask task);

    SkillGuidance buildGuidance(AgentTask task);

    SuccessContract enrichContract(AgentTask task, SuccessContract baseContract);
}
