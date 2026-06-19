package com.bvz.aiagent.core.contract;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactContractTest {

    @Test
    void shouldCarryArtifactConstraints() {
        ArtifactContract contract = new ArtifactContract(
                "PDF",
                true,
                true,
                true,
                List.of("概览", "地点推荐", "行动建议"),
                List.of("pdf")
        );

        assertEquals("PDF", contract.artifactType());
        assertTrue(contract.mustExist());
        assertTrue(contract.mustBeReadable());
        assertTrue(contract.mustUseLocalAssets());
        assertEquals(List.of("概览", "地点推荐", "行动建议"), contract.requiredSections());
        assertEquals(List.of("pdf"), contract.allowedFormats());
    }

    @Test
    void shouldRejectBlankArtifactType() {
        assertThrows(IllegalArgumentException.class, () -> new ArtifactContract(
                " ",
                true,
                true,
                false,
                List.of(),
                List.of("pdf")
        ));
    }
}
