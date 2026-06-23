package com.bvz.aiagent.domain.love.skills;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;
import com.bvz.aiagent.core.skill.SkillGuidance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageDraftSkillTest {

    private final MessageDraftSkill skill = new MessageDraftSkill();

    @Test
    void shouldMatchApologyConfessionAndCommunicationScriptScenarios() {
        assertTrue(skill.matches(new AgentTask("帮我写一段道歉消息")));
        assertTrue(skill.matches(new AgentTask("帮我写一段表白文案")));
        assertTrue(skill.matches(new AgentTask("帮我写一个和对象沟通的对话脚本")));
    }

    @Test
    void shouldConstrainToneAndStructureWithoutExternalToolsByDefault() {
        SuccessContract base = new SuccessContract(false, false, null, null, true, false, false);

        SkillGuidance guidance = skill.buildGuidance(new AgentTask("给我写一段真诚但不过度卑微的道歉消息"));
        SuccessContract enriched = skill.enrichContract(new AgentTask("给我写一段真诚但不过度卑微的道歉消息"), base);

        assertTrue(guidance.promptFragments().stream().anyMatch(fragment -> fragment.contains("语气")));
        assertTrue(guidance.promptFragments().stream().anyMatch(fragment -> fragment.contains("长度")));
        assertFalse(guidance.recommendedCapabilities().stream().findAny().isPresent());
        assertFalse(enriched.requiresExternalEvidence());
        assertFalse(enriched.requiresArtifact());
    }
}
