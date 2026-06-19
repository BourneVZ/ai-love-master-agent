package com.bvz.aiagent.core.tool.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGenerationResultTest {

    @Test
    void shouldCarrySuccessReadabilityAndEmbeddedLocalAssets() {
        PdfGenerationResult result = new PdfGenerationResult(
                true,
                "/tmp/pdf/date-plan.pdf",
                true,
                List.of("/tmp/download/a.jpg", "/tmp/download/b.jpg")
        );

        assertTrue(result.success());
        assertEquals("/tmp/pdf/date-plan.pdf", result.localPath());
        assertTrue(result.readable());
        assertEquals(2, result.embeddedLocalAssets().size());
    }

    @Test
    void shouldRejectSuccessfulPdfWithoutLocalPath() {
        assertThrows(IllegalArgumentException.class, () -> new PdfGenerationResult(true, "", true, List.of()));
    }
}
