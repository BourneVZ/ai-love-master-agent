package com.bvz.aiagent.domain.love.skills;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.TaskType;
import com.bvz.aiagent.core.skill.SkillGuidance;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelationshipAdviceSkillTest {

    private final RelationshipAdviceSkill skill = new RelationshipAdviceSkill();

    @Test
    void shouldMatchEmotionalSupportRelationshipAnalysisAndCommunicationAdvice() {
        AgentTask supportTask = new AgentTask(
                "task-1",
                "我和对象吵架了，安慰我一下",
                "安慰并提供情绪支持",
                TaskType.ADVICE,
                "LOW",
                false,
                false,
                Map.of(),
                Map.of()
        );
        AgentTask analysisTask = new AgentTask("帮我分析一下这段关系的问题");
        AgentTask communicationTask = new AgentTask("给我一些和另一半沟通的建议");

        assertTrue(skill.matches(supportTask));
        assertTrue(skill.matches(analysisTask));
        assertTrue(skill.matches(communicationTask));
    }

    @Test
    void shouldEnhanceEmpathyStructureAndSafetyBoundariesWithoutRequiringTools() {
        SuccessContract base = new SuccessContract(false, false, null, null, true, false, false);

        SkillGuidance guidance = skill.buildGuidance(new AgentTask("我和对象关系很紧张，怎么沟通"));
        SuccessContract enriched = skill.enrichContract(new AgentTask("我和对象关系很紧张，怎么沟通"), base);

        assertTrue(guidance.promptFragments().stream().anyMatch(fragment -> fragment.contains("共情")));
        assertTrue(guidance.promptFragments().stream().anyMatch(fragment -> fragment.contains("风险")));
        assertTrue(guidance.recommendedCapabilities().isEmpty());
        assertFalse(enriched.requiresExternalEvidence());
        assertFalse(enriched.requiresArtifact());
    }
}
