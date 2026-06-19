package com.bvz.aiagent.core.tool.interpreter;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bvz.aiagent.core.tool.ToolResultInterpreter;
import com.bvz.aiagent.core.tool.model.PdfGenerationResult;

import java.util.ArrayList;
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
        if (rawResult != null && JSONUtil.isTypeJSON(rawResult)) {
            JSONObject json = JSONUtil.parseObj(rawResult);
            JSONArray assets = json.getJSONArray("embeddedLocalAssets");
            List<String> embeddedLocalAssets = new ArrayList<>();
            if (assets != null) {
                assets.forEach(item -> embeddedLocalAssets.add(String.valueOf(item)));
            }
            return new PdfGenerationResult(
                    json.getBool("success", false),
                    json.getStr("localPath", ""),
                    json.getBool("readable", false),
                    embeddedLocalAssets
            );
        }

        String prefix = "PDF generated successfully to: ";
        if (rawResult != null && rawResult.startsWith(prefix)) {
            return new PdfGenerationResult(true, rawResult.substring(prefix.length()), true, List.of());
        }
        return new PdfGenerationResult(false, "", false, List.of());
    }
}
