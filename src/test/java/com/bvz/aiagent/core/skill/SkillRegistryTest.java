package com.bvz.aiagent.core.skill;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillRegistryTest {

    @Test
    void shouldMatchSkillsByTaskAndPreserveRegistrationOrder() {
        Skill dateSkill = new StubSkill("date-plan", true, new SkillGuidance(List.of("date"), List.of("searchWeb"), null), null);
        Skill adviceSkill = new StubSkill("relationship-advice", false, new SkillGuidance(List.of("advice"), List.of(), null), null);
        Skill messageSkill = new StubSkill("message-draft", true, new SkillGuidance(List.of("message"), List.of(), null), null);
        SkillRegistry registry = new SkillRegistry(List.of(dateSkill, adviceSkill, messageSkill));

        List<Skill> matched = registry.matchingSkills(new AgentTask("帮我规划约会并写一段邀请话术"));

        assertEquals(List.of(dateSkill, messageSkill), matched);
    }

    @Test
    void shouldMergeGuidanceAndContractEnrichmentAcrossMatchedSkills() {
        SuccessContract base = new SuccessContract(false, false, null, null, true, false, false);
        SuccessContract enriched = new SuccessContract(true, false, null, null, true, false, false);
        SkillRegistry registry = new SkillRegistry(List.of(
                new StubSkill("date-plan", true, new SkillGuidance(List.of("先确认地点范围"), List.of("searchWeb"), null), enriched),
                new StubSkill("message-draft", true, new SkillGuidance(List.of("控制语气自然"), List.of(), null), null)
        ));

        SkillRegistry.GuidanceBundle bundle = registry.buildGuidance(new AgentTask("给我约会邀请文案"), base);

        assertEquals(List.of("先确认地点范围", "控制语气自然"), bundle.promptFragments());
        assertEquals(List.of("searchWeb"), bundle.recommendedCapabilities());
        assertEquals(enriched, bundle.contract());
        assertTrue(bundle.matchedSkillIds().contains("date-plan"));
        assertTrue(bundle.matchedSkillIds().contains("message-draft"));
    }

    private record StubSkill(
            String id,
            boolean matches,
            SkillGuidance guidance,
            SuccessContract enrichedContract
    ) implements Skill {

        @Override
        public boolean matches(AgentTask task) {
            return matches;
        }

        @Override
        public SkillGuidance buildGuidance(AgentTask task) {
            return guidance;
        }

        @Override
        public SuccessContract enrichContract(AgentTask task, SuccessContract baseContract) {
            return enrichedContract == null ? baseContract : enrichedContract;
        }
    }
}
