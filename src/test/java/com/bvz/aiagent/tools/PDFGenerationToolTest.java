package com.bvz.aiagent.tools;

import com.bvz.aiagent.core.tool.interpreter.PdfGenerationResultInterpreter;
import com.bvz.aiagent.core.tool.model.PdfGenerationResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PDFGenerationToolTest {

    @Test
    void shouldProduceStructuredPdfOutputWithReadableArtifactAndEmbeddedLocalAssets() throws Exception {
        Path imagePath = Path.of(FileConstant.FILE_SAVE_DIR, "download", "phase8-embedded.png");
        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+yF9kAAAAASUVORK5CYII="
        ));

        PDFGenerationTool tool = new PDFGenerationTool();
        String raw = tool.generatePDF(
                "phase8-structured.pdf",
                "# Date Plan\n![cover](" + imagePath.toAbsolutePath() + ")"
        );

        PdfGenerationResult result = new PdfGenerationResultInterpreter().interpret(raw);

        assertTrue(result.success());
        assertTrue(result.readable());
        assertTrue(result.localPath().endsWith("phase8-structured.pdf"));
        assertTrue(result.embeddedLocalAssets().contains(imagePath.toAbsolutePath().toString()));
    }

    @Test
    void shouldCleanBrokenFilesAndReturnStructuredFailureWhenPdfGenerationFails() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String raw = tool.generatePDF("bad:name.pdf", "content");

        PdfGenerationResult result = new PdfGenerationResultInterpreter().interpret(raw);

        assertFalse(result.success());
        assertFalse(result.readable());
    }
}
