package com.bvz.aiagent.core.skill;

import com.bvz.aiagent.core.contract.SuccessContract;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillGuidanceTest {

    @Test
    void shouldCarryPromptFragmentsRecommendedCapabilitiesAndContractEnrichment() {
        SuccessContract enrichedContract = new SuccessContract(true, false, null, null, true, false, false);
        SkillGuidance guidance = new SkillGuidance(
                List.of("先明确用户约会偏好", "优先补充地点覆盖"),
                List.of("searchWeb", "searchImage", "generatePDF"),
                enrichedContract
        );

        assertEquals(List.of("先明确用户约会偏好", "优先补充地点覆盖"), guidance.promptFragments());
        assertEquals(List.of("searchWeb", "searchImage", "generatePDF"), guidance.recommendedCapabilities());
        assertEquals(enrichedContract, guidance.enrichedContract());
    }

    @Test
    void shouldRejectEmptyPromptFragmentsAndCapabilitiesWhenProvided() {
        assertThrows(IllegalArgumentException.class, () -> new SkillGuidance(
                List.of(""),
                List.of("searchWeb"),
                null
        ));
        assertThrows(IllegalArgumentException.class, () -> new SkillGuidance(
                List.of("valid"),
                List.of(" "),
                null
        ));
    }
}
