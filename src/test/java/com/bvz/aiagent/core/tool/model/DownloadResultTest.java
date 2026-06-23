package com.bvz.aiagent.core.tool.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadResultTest {

    @Test
    void shouldCarrySuccessFlagLocalPathMimeTypeAndSize() {
        DownloadResult result = new DownloadResult(true, "/tmp/download/a.png", "image/png", 1024L);

        assertTrue(result.success());
        assertEquals("/tmp/download/a.png", result.localPath());
        assertEquals("image/png", result.mimeType());
        assertEquals(1024L, result.size());
    }

    @Test
    void shouldRejectSuccessfulResultWithoutLocalPath() {
        assertThrows(IllegalArgumentException.class, () -> new DownloadResult(true, " ", "image/png", 10L));
    }
}
