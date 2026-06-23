package com.bvz.aiagent.core.tool.model;

public record DownloadResult(
        boolean success,
        String localPath,
        String mimeType,
        long size
) {

    public DownloadResult {
        mimeType = mimeType == null ? "" : mimeType;
        if (success && (localPath == null || localPath.isBlank())) {
            throw new IllegalArgumentException("localPath must not be blank when success");
        }
        localPath = localPath == null ? "" : localPath;
    }
}
