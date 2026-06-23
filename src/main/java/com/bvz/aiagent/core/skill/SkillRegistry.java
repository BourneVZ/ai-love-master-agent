package com.bvz.aiagent.core.skill;

import com.bvz.aiagent.core.contract.SuccessContract;
import com.bvz.aiagent.core.model.AgentTask;

import java.util.ArrayList;
import java.util.List;

public class SkillRegistry {

    private final List<Skill> skills;

    public SkillRegistry(List<Skill> skills) {
        this.skills = List.copyOf(skills);
    }

    public List<Skill> matchingSkills(AgentTask task) {
        return skills.stream()
                .filter(skill -> skill.matches(task))
                .toList();
    }

    public GuidanceBundle buildGuidance(AgentTask task, SuccessContract baseContract) {
        List<String> promptFragments = new ArrayList<>();
        List<String> recommendedCapabilities = new ArrayList<>();
        List<String> matchedSkillIds = new ArrayList<>();
        SuccessContract contract = baseContract;

        for (Skill skill : matchingSkills(task)) {
            matchedSkillIds.add(skill.id());
            SkillGuidance guidance = skill.buildGuidance(task);
            promptFragments.addAll(guidance.promptFragments());
            recommendedCapabilities.addAll(guidance.recommendedCapabilities());
            contract = skill.enrichContract(task, contract);
        }

        return new GuidanceBundle(
                List.copyOf(promptFragments),
                List.copyOf(recommendedCapabilities),
                contract,
                List.copyOf(matchedSkillIds)
        );
    }

    public record GuidanceBundle(
            List<String> promptFragments,
            List<String> recommendedCapabilities,
            SuccessContract contract,
            List<String> matchedSkillIds
    ) {
    }
}
