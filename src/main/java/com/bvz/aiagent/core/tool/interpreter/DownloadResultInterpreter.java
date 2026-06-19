package com.bvz.aiagent.core.tool.interpreter;

import com.bvz.aiagent.core.tool.ToolResultInterpreter;
import com.bvz.aiagent.core.tool.model.DownloadResult;

public class DownloadResultInterpreter implements ToolResultInterpreter<DownloadResult> {

    @Override
    public String toolName() {
        return "downloadResource";
    }

    @Override
    public Class<DownloadResult> resultType() {
        return DownloadResult.class;
    }

    @Override
    public DownloadResult interpret(String rawResult) {
        String prefix = "Resource downloaded successfully to: ";
        if (rawResult != null && rawResult.startsWith(prefix)) {
            return new DownloadResult(true, rawResult.substring(prefix.length()), "", 0L);
        }
        return new DownloadResult(false, "", "", 0L);
    }
}
