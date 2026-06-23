package com.bvz.aiagent.core.tool.interpreter;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.bvz.aiagent.core.tool.ToolResultInterpreter;
import com.bvz.aiagent.core.tool.model.ImageSearchResult;

import java.util.ArrayList;
import java.util.List;

public class ImageSearchResultInterpreter implements ToolResultInterpreter<ImageSearchResult> {

    @Override
    public String toolName() {
        return "searchImage";
    }

    @Override
    public Class<ImageSearchResult> resultType() {
        return ImageSearchResult.class;
    }

    @Override
    public ImageSearchResult interpret(String rawResult) {
        JSONArray array = JSONUtil.parseArray("[" + rawResult + "]");
        List<ImageSearchResult.ImageItem> items = new ArrayList<>();
        array.forEach(item -> {
            cn.hutool.json.JSONObject json = (cn.hutool.json.JSONObject) item;
            items.add(new ImageSearchResult.ImageItem(
                    json.getStr("title"),
                    json.getStr("imageUrl"),
                    json.getStr("sourceUrl")
            ));
        });
        return new ImageSearchResult(items);
    }
}
