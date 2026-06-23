package com.bvz.aiagent.core.contract;

public record SuccessContract(
        boolean requiresExternalEvidence,
        boolean requiresArtifact,
        ArtifactContract artifactContract,
        CoverageContract coverageContract,
        boolean truthfulnessRequired,
        boolean localAssetRequired,
        boolean forbiddenExtraArtifacts
) {

    public SuccessContract {
        if (requiresArtifact && artifactContract == null) {
            throw new IllegalArgumentException("artifactContract must be provided when artifact is required");
        }
    }
}
