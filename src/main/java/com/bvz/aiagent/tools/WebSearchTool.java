package com.bvz.aiagent.tools;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;
    private final OkHttpClient client;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    @Tool(description = "使用百度搜索网页信息，适合查找地点、攻略、资讯等外部资料")
    public String searchWeb(@ToolParam(description = "搜索关键词") String query) {
        HttpUrl.Builder urlBuilder = HttpUrl.get(SEARCH_API_URL).newBuilder();
        urlBuilder.addQueryParameter("engine", "baidu");
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("api_key", apiKey);

        Request request = new Request.Builder().url(urlBuilder.build()).build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject jsonObject = JSONUtil.parseObj(responseBody);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            int endIndex = Math.min(organicResults.size(), 5);
            List<Object> items = organicResults.subList(0, endIndex).stream().map(obj -> {
                JSONObject item = (JSONObject) obj;
                return JSONUtil.createObj()
                        .set("title", item.getStr("title"))
                        .set("snippet", item.getStr("snippet"))
                        .set("link", item.getStr("link"));
            }).collect(Collectors.toList());
            return JSONUtil.createObj().set("items", items).toString();
        } catch (IOException e) {
            return JSONUtil.createObj()
                    .set("items", JSONUtil.createArray())
                    .set("error", e.getMessage())
                    .toString();
        }
    }
}
