package com.bvz.aiagent.core.skill;

import com.bvz.aiagent.core.contract.SuccessContract;

import java.util.List;

public record SkillGuidance(
        List<String> promptFragments,
        List<String> recommendedCapabilities,
        SuccessContract enrichedContract
) {

    public SkillGuidance {
        promptFragments = List.copyOf(promptFragments);
        recommendedCapabilities = List.copyOf(recommendedCapabilities);

        if (promptFragments.stream().anyMatch(fragment -> fragment == null || fragment.isBlank())) {
            throw new IllegalArgumentException("promptFragments must not contain blank values");
        }
        if (recommendedCapabilities.stream().anyMatch(capability -> capability == null || capability.isBlank())) {
            throw new IllegalArgumentException("recommendedCapabilities must not contain blank values");
        }
    }
}
