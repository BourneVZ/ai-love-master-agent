package com.bvz.aiagent.core.contract;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuccessContractTest {

    @Test
    void shouldCombineEvidenceArtifactAndTruthfulnessRequirements() {
        ArtifactContract artifactContract = new ArtifactContract(
                "PDF",
                true,
                true,
                true,
                List.of("行程"),
                List.of("pdf")
        );
        CoverageContract coverageContract = new CoverageContract("DATE_SPOTS", 5, 0.8, 1);

        SuccessContract contract = new SuccessContract(
                true,
                true,
                artifactContract,
                coverageContract,
                true,
                true,
                true
        );

        assertTrue(contract.requiresExternalEvidence());
        assertTrue(contract.requiresArtifact());
        assertEquals(artifactContract, contract.artifactContract());
        assertEquals(coverageContract, contract.coverageContract());
        assertTrue(contract.truthfulnessRequired());
        assertTrue(contract.localAssetRequired());
        assertTrue(contract.forbiddenExtraArtifacts());
    }

    @Test
    void shouldRejectArtifactRequirementWithoutArtifactContract() {
        assertThrows(IllegalArgumentException.class, () -> new SuccessContract(
                false,
                true,
                null,
                null,
                true,
                false,
                true
        ));
    }
}
