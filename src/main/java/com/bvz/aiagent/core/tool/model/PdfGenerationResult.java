package com.bvz.aiagent.core.tool.model;

import java.util.List;

public record PdfGenerationResult(
        boolean success,
        String localPath,
        boolean readable,
        List<String> embeddedLocalAssets
) {

    public PdfGenerationResult {
        embeddedLocalAssets = List.copyOf(embeddedLocalAssets);
        if (success && (localPath == null || localPath.isBlank())) {
            throw new IllegalArgumentException("localPath must not be blank when success");
        }
        localPath = localPath == null ? "" : localPath;
    }
}
