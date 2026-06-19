package com.bvz.aiagent.core.tool.interpreter;

import com.bvz.aiagent.core.tool.ToolResultInterpreter;
import com.bvz.aiagent.core.tool.model.PdfGenerationResult;

import java.util.List;

public class PdfGenerationResultInterpreter implements ToolResultInterpreter<PdfGenerationResult> {

    @Override
    public String toolName() {
        return "generatePDF";
    }

    @Override
    public Class<PdfGenerationResult> resultType() {
        return PdfGenerationResult.class;
    }

    @Override
    public PdfGenerationResult interpret(String rawResult) {
        String prefix = "PDF generated successfully to: ";
        if (rawResult != null && rawResult.startsWith(prefix)) {
            return new PdfGenerationResult(true, rawResult.substring(prefix.length()), true, List.of());
        }
        return new PdfGenerationResult(false, "", false, List.of());
    }
}
