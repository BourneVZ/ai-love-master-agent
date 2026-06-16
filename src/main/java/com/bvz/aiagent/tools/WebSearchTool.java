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

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;
    private final OkHttpClient client;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        HttpUrl.Builder urlBuilder = HttpUrl.get(SEARCH_API_URL).newBuilder();
        urlBuilder.addQueryParameter("engine", "baidu");
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("api_key", apiKey);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            // 取出返回结果的前 5 条
            JSONObject jsonObject = JSONUtil.parseObj(responseBody);
            // 提取 organic_results 部分
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            int endIndex = Math.min(organicResults.size(), 5);
            List<Object> objects = organicResults.subList(0, endIndex);
            // 拼接搜索结果为字符串
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (IOException e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}
