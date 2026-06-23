package com.bvz.aiagent.core.contract;

public record CoverageContract(
        String subjectType,
        int expectedCount,
        double minCoverageRatio,
        int perItemImageMinCount
) {

    public CoverageContract {
        if (subjectType == null || subjectType.isBlank()) {
            throw new IllegalArgumentException("subjectType must not be blank");
        }
        if (expectedCount <= 0) {
            throw new IllegalArgumentException("expectedCount must be positive");
        }
        if (minCoverageRatio < 0 || minCoverageRatio > 1) {
            throw new IllegalArgumentException("minCoverageRatio must be between 0 and 1");
        }
        if (perItemImageMinCount < 0) {
            throw new IllegalArgumentException("perItemImageMinCount must not be negative");
        }
    }
}
