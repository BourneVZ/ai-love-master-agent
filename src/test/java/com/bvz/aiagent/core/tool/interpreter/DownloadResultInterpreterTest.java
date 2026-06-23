package com.bvz.aiagent.core.tool.interpreter;

import com.bvz.aiagent.core.tool.model.DownloadResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadResultInterpreterTest {

    @Test
    void shouldInterpretDownloadResourceRawResultIntoStructuredModel() {
        String raw = "Resource downloaded successfully to: /tmp/download/logo.png";

        DownloadResult result = new DownloadResultInterpreter().interpret(raw);

        assertTrue(result.success());
        assertEquals("/tmp/download/logo.png", result.localPath());
    }
}
