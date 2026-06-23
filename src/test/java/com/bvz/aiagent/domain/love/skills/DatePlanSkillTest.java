package com.bvz.aiagent.domain.love.skills;

import com.bvz.aiagent.core.contract.ArtifactContract;
import com.bvz.aiagent.core.contract.CoverageContract;
import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.model.TaskType;
import com.bvz.aiagent.core.skill.SkillGuidance;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatePlanSkillTest {

    private final DatePlanSkill skill = new DatePlanSkill();

    @Test
    void shouldMatchDateRecommendationAndDatePlanScenarios() {
        AgentTask recommendationTask = new AgentTask(
                "task-1",
                "推荐几个适合情侣约会的地点",
                "推荐情侣约会地点",
                TaskType.RESEARCH,
                "MEDIUM",
                false,
                true,
                Map.of(),
                Map.of()
        );
        AgentTask planTask = new AgentTask(
                "task-2",
                "帮我制定一个七夕约会计划",
                "制定七夕约会计划",
                TaskType.ARTIFACT,
                "MEDIUM",
                true,
                true,
                Map.of(),
                Map.of()
        );

        assertTrue(skill.matches(recommendationTask));
        assertTrue(skill.matches(planTask));
    }

    @Test
    void shouldEnrichContractWithCoverageArtifactAndRecommendedCapabilities() {
        SuccessContract base = new SuccessContract(false, false, null, null, true, false, false);
        AgentTask task = new AgentTask("帮我生成带图片的约会计划 PDF");

        SkillGuidance guidance = skill.buildGuidance(task);
        SuccessContract enriched = skill.enrichContract(task, base);

        assertTrue(guidance.promptFragments().stream().anyMatch(fragment -> fragment.contains("地点")));
        assertTrue(guidance.recommendedCapabilities().contains("searchWeb"));
        assertTrue(guidance.recommendedCapabilities().contains("searchImage"));
        assertTrue(guidance.recommendedCapabilities().contains("generatePDF"));
        assertTrue(enriched.requiresExternalEvidence());
        assertTrue(enriched.requiresArtifact());
        assertTrue(enriched.artifactContract() instanceof ArtifactContract);
        assertTrue(enriched.coverageContract() instanceof CoverageContract);
        assertEquals("PDF", enriched.artifactContract().artifactType());
    }
}
