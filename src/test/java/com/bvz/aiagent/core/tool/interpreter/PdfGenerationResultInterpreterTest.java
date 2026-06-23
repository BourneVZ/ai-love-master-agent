package com.bvz.aiagent.core.tool.interpreter;

import com.bvz.aiagent.core.tool.model.PdfGenerationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGenerationResultInterpreterTest {

    @Test
    void shouldInterpretGeneratePdfRawResultIntoStructuredModel() {
        String raw = "PDF generated successfully to: /tmp/pdf/date-plan.pdf";

        PdfGenerationResult result = new PdfGenerationResultInterpreter().interpret(raw);

        assertTrue(result.success());
        assertEquals("/tmp/pdf/date-plan.pdf", result.localPath());
        assertTrue(result.readable());
    }
}
