package com.bvz.aiagent.core.tool;

public record ToolDescriptor(
        String name,
        ToolCapability capabilityType,
        SideEffectLevel sideEffectLevel,
        String inputSchema,
        String outputSchema,
        boolean supportsRetry,
        boolean artifactProducing
) {

    public ToolDescriptor {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (capabilityType == null) {
            throw new IllegalArgumentException("capabilityType must not be null");
        }
        if (sideEffectLevel == null) {
            throw new IllegalArgumentException("sideEffectLevel must not be null");
        }
    }

    public enum SideEffectLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }
}
