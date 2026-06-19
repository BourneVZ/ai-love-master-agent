package com.bvz.aiagent.tools;

import com.bvz.aiagent.agent.TerminateTool;
import com.bvz.aiagent.core.tool.ToolCapability;
import com.bvz.aiagent.core.tool.ToolDescriptor;
import com.bvz.aiagent.core.tool.ToolResultInterpreterRegistry;
import com.bvz.aiagent.core.tool.interpreter.DownloadResultInterpreter;
import com.bvz.aiagent.core.tool.interpreter.ImageSearchResultInterpreter;
import com.bvz.aiagent.core.tool.interpreter.PdfGenerationResultInterpreter;
import com.bvz.aiagent.core.tool.interpreter.WebSearchResultInterpreter;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
        );
    }

    public Map<String, ToolDescriptor> toolDescriptors() {
        return Map.of(
                "searchWeb", new ToolDescriptor("searchWeb", ToolCapability.SEARCH, ToolDescriptor.SideEffectLevel.LOW, "query", "web-search-result", true, false),
                "downloadResource", new ToolDescriptor("downloadResource", ToolCapability.DOWNLOAD, ToolDescriptor.SideEffectLevel.HIGH, "url,fileName", "download-result", true, false),
                "generatePDF", new ToolDescriptor("generatePDF", ToolCapability.ARTIFACT_GENERATION, ToolDescriptor.SideEffectLevel.HIGH, "fileName,content", "pdf-generation-result", true, true)
        );
    }

    @Bean
    public ToolResultInterpreterRegistry toolResultInterpreterRegistry() {
        return new ToolResultInterpreterRegistry(List.of(
                new WebSearchResultInterpreter(),
                new ImageSearchResultInterpreter(),
                new DownloadResultInterpreter(),
                new PdfGenerationResultInterpreter()
        ));
    }
}
