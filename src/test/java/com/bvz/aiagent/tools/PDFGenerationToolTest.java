package com.bvz.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "生而为人.pdf";
        String content = "觉照 正念 处一 幽默 侵略";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
