package com.bvz.aiagent.core.tool;

public enum ToolCapability {
    SEARCH(false),
    WEB_SCRAPING(true),
    DOWNLOAD(true),
    FILE_OPERATION(true),
    TERMINAL_EXECUTION(true),
    ARTIFACT_GENERATION(true),
    TASK_TERMINATION(false);

    private final boolean highRisk;

    ToolCapability(boolean highRisk) {
        this.highRisk = highRisk;
    }

    public boolean isHighRisk() {
        return highRisk;
    }
}
