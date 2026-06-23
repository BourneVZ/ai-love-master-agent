package com.bvz.aiagent.core.contract;

import java.util.List;

public record ArtifactContract(
        String artifactType,
        boolean mustExist,
        boolean mustBeReadable,
        boolean mustUseLocalAssets,
        List<String> requiredSections,
        List<String> allowedFormats
) {

    public ArtifactContract {
        if (artifactType == null || artifactType.isBlank()) {
            throw new IllegalArgumentException("artifactType must not be blank");
        }
        requiredSections = List.copyOf(requiredSections);
        allowedFormats = List.copyOf(allowedFormats);
    }
}
