package com.bvz.aiagent.core.tool.interpreter;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bvz.aiagent.core.tool.ToolResultInterpreter;
import com.bvz.aiagent.core.tool.model.WebSearchResult;

import java.util.ArrayList;
import java.util.List;

public class WebSearchResultInterpreter implements ToolResultInterpreter<WebSearchResult> {

    @Override
    public String toolName() {
        return "searchWeb";
    }

    @Override
    public Class<WebSearchResult> resultType() {
        return WebSearchResult.class;
    }

    @Override
    public WebSearchResult interpret(String rawResult) {
        if (rawResult == null || rawResult.isBlank()) {
            return new WebSearchResult(List.of());
        }

        JSONArray array;
        String normalized = rawResult.trim();
        if (normalized.startsWith("{") && normalized.contains("\"items\"")) {
            JSONObject json = JSONUtil.parseObj(rawResult);
            array = json.getJSONArray("items");
            if (array == null) {
                return new WebSearchResult(List.of());
            }
        } else {
            array = JSONUtil.parseArray("[" + rawResult + "]");
        }

        List<WebSearchResult.SearchItem> items = new ArrayList<>();
        array.forEach(item -> {
            JSONObject json = (JSONObject) item;
            items.add(new WebSearchResult.SearchItem(
                    json.getStr("title"),
                    json.getStr("snippet"),
                    json.getStr("link")
            ));
        });
        return new WebSearchResult(items);
    }
}
